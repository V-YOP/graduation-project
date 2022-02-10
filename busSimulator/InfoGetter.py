import threading
import requests
import json
import pymysql
pymysql.install_as_MySQLdb()
conn = pymysql.Connect(
        host = 'localhost',
        port = 3306,
        user = 'root',
        passwd = 'aaaaaaaa',
        db = 'yukinaDB',
        charset = 'utf8')
cursor = conn.cursor()

def getAllBusInfo():
    sql = "CALL getBusInfo(CURRENT_DATE)"
    cursor.execute(sql)
    res = []
    for row in cursor.fetchall():
        bus_id = row[1]
        data_json = row[2]
        res.append({"bus_id": bus_id, "data": json.loads(data_json)["data"]})
    return res

def getNodeInfo(node_id):
    sql = "SELECT * FROM node WHERE node_id={} AND CURRENT_DATE() BETWEEN start_time AND end_time".format(node_id)
    cursor.execute(sql)
    res = cursor.fetchone()
    return {"node_id":node_id,"lat":res[1], "lont": res[2]}

def getNodepathInfo(nodepath_id):
    sql = "SELECT * FROM nodepath WHERE nodepath_id={} AND CURRENT_DATE() BETWEEN start_time AND end_time".format(nodepath_id)
    cursor.execute(sql)
    res = cursor.fetchone()
    return {"nodepath_id": nodepath_id, "node1_id": res[1], "node2_id": res[2], "speed_limit": res[3], "direction": res[4], "street_name": res[5]}

# TODO
def getAllInfo():
    res = []
    for bus in getAllBusInfo():
        res.append(parseBusInfo(bus))
    return res

def parseBusInfo(bus):
    bus_id = bus["bus_id"]
    nodeList = [] # {"lat":..,"lont":..,station?}
    speedLimitList = [] # 保存各个路段的限速，speedLimitList[i]为nodeList[i]和nodeList[i+1]之间的限速
    obj = bus["data"]
    start_node = getNodeInfo(obj[0][0])
    nodeList.append({"node_id": start_node["node_id"],"lat": start_node["lat"], "lont": start_node["lont"], "isStation":obj[0][1]})
    for item in obj[1:]:
        nodepath_id = item[0]
        nextNodeIsStation = item[1]
        lastNode = nodeList[-1]
        nodepath = getNodepathInfo(nodepath_id)
        if ((nodepath["direction"] == 0 and nodepath["node1_id"] != lastNode["node_id"]) or 
            (nodepath["direction"] == 1 and nodepath["node2_id"] != lastNode["node_id"])):
            raise Exception("id为{}的nodepath的数据有问题！")
        nextNode_id = nodepath["node2_id"] if nodepath["node1_id"] == lastNode["node_id"] else nodepath["node1_id"]
        nextNodeInfo = getNodeInfo(nextNode_id)
        nodeList.append({"node_id": nextNode_id, "lat": nextNodeInfo["lat"], "lont": nextNodeInfo["lont"], "isStation":nextNodeIsStation})
        speedLimitList.append(nodepath["speed_limit"])
    return {"bus_id": bus_id, "nodeList": nodeList, "speedLimitList":speedLimitList}
    
def main():
    tmp = getAllInfo()
    
        
if __name__=="__main__":
    main()