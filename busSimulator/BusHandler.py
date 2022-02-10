# TODO 要添加更有趣的功能
from LatLont import getDistance
import math
import time
import json
from enum import Enum
import random
# speed单位是km/h，转换成m/s需要除以3.6

# 各种状态，主要是用来调整速度的
state = Enum('state', 
"""
    NORMAL 
    MAKINGTROBLE
    SPEEDDOWN
""")

def Handler(data_):
    bus_id = data_["bus_id"]
    line_id = data_["line_id"]
    nodeList = data_["nodeList"][:]
    speedLimitList = data_["speedLimitList"]
    startNode = nodeList[0]
    nextNode = nodeList[1]
    startLat = startNode["lat"]
    startLont = startNode["lont"]
    nextNodeLat = nextNode["lat"]
    nextNodeLont = nextNode["lont"]
    # * data其实就是bus所持有的数据，这里也可以使用对象来实现
    data = {
        "nodeList": nodeList,
        "speedLimitList": speedLimitList,
        "nodeAfterIndex": 1, # 标识下一个node的index
        "lat": startLat,
        "lont": startLont,
        "nextNodeLat": nextNodeLat,
        "nextNodeLont": nextNodeLont,
        "speed": 0, 
        "countDown": 30, # * 状态持续的倒计时，如果为0则更换状态且重置倒计时
        "state": state.NORMAL
    }
    oriData = data.copy() # ! 必须要copy才行
    def fn():
        nonlocal data
        msg = {
            "bus_id": bus_id, 
            "line_id":line_id ,
            "lat": float(data["lat"]), 
            "lont": float(data["lont"]), 
            "speed": int(data["speed"]),
            "update_time": int(round(time.time() * 1000))
        }
        data = next(data, oriData) 
        return json.dumps(msg);
    return fn

def next(data, oriData):

    # * 说明到终点了，直接从起点重新开始
    if data["nodeAfterIndex"] >= len(data["nodeList"]):
        return oriData
    nextNodeIndex = data["nodeAfterIndex"]
    nextNode = data["nodeList"][nextNodeIndex]
    nextNodeLat = nextNode["lat"]
    nextNodeLont = nextNode["lont"]
    nextLat, nextLont = nextPos(data["lat"],data["lont"],data["nextNodeLat"], data["nextNodeLont"], data["speed"])
    distanceToNextNode = getDistance(nextLat,nextLont,nextNodeLat,nextNodeLont)

    # 一个纯粹利用副作用的函数……废话。不是利用副作用就是利用返回值，这里直接丢弃了返回值
    adjustStateAndSpeed(data , distanceToNextNode)

    # * 规定与下一个node距离小于20m则为下一个站点
    # * 这大概能保证在正常情况下不会越界…
    if distanceToNextNode < 20:
        if nextNodeIndex+1 >= len(data["nodeList"]):
            return oriData
        nextnextNode = data["nodeList"][nextNodeIndex+1]

        # ! 更换目的地，重置状态
        return {
            "nodeList": data["nodeList"],
            "speedLimitList": data["speedLimitList"],
            "nodeAfterIndex": nextNodeIndex + 1,
            "lat": nextNodeLat,
            "lont": nextNodeLont,
            "nextNodeLat": nextnextNode["lat"],
            "nextNodeLont": nextnextNode["lont"],
            "speed": data["speed"],
            "countDown" : data["countDown"],
            "state": data["state"]
        }
    # *更新了data的值后直接返回data，这里应当不会出问题。

    data["lat"] = nextLat
    data["lont"] = nextLont
    return data

def nextPos(lat,lont,nextNodeLat,nextNodeLont,speed):
    distance = speed / 3.6 # 1秒的距离
    fullDistance = getDistance(lat,lont,nextNodeLat,nextNodeLont)
    scope = None
    if fullDistance == 0:
        scope = 0.001
    else:
        scope = distance / fullDistance        
    latMinus = float(nextNodeLat) - float(lat) 
    lontMinus = float(nextNodeLont) - float(lont) 
    return (float(lat) + float(latMinus) * scope, float(lont) + float(lontMinus) * scope)
# 

# dist是距下个节点的距离，这里其实可以直接计算的，但是懒了。
def adjustStateAndSpeed(data , dist):
    # 无论如何也不能让速度变成负数
    if data["speed"] <= 0:
        data["speed"] = abs(data["speed"])
    presentSpeed = data["speed"]
    speedLimit = data["speedLimitList"][data["nodeAfterIndex"] - 1]
    # 在这些子函数里只更改速度，其它什么都不更改
    # 令这之间的速度为正常范围
    overspeed = presentSpeed > speedLimit - 5 # 假设在小于限速5的时候就超速
    tooSlow = presentSpeed < speedLimit - 20
    if tooSlow < 0 : tooSlow = 0
    def normal():
        # 保持速度到[speedLimit - 20, speedLimit)
        if overspeed:
            data["speed"] -= 5
        elif tooSlow:
            data["speed"] += 5
        else:
            # 正常情况，速度随机增减
            distToOverspeed = speedLimit - presentSpeed
            distToSlow = presentSpeed - speedLimit + 20
            # 离超速越近，速度减慢可能性越高，离低速越近，速度增加可能性越高
            adjustSpeed = math.floor(random.random() * 5)
            if (random.random() > distToSlow/(distToSlow+distToOverspeed)):
                data["speed"] -= adjustSpeed
            else:
                data["speed"] += adjustSpeed
    def makingtroble():
        # 不对数据进行任何限制，速度在[0,120)之间就行
        data["speed"] += (random.random()-0.5) * 30
        if data["speed"] > 120:
            data["speed"] -= 20 
        elif data["speed"] < 0:
            data["speed"] = abs(data["speed"])
    def speeddown():
        #TODO 尽量减速
        if (data["speed"] > 15):
            data["speed"] -= 5
        if (data["speed"] <= 1):
            data["speed"] += 10
        pass 
    opt = {
        state.NORMAL: normal, # 正常
        state.MAKINGTROBLE: makingtroble, # 捣乱
        state.SPEEDDOWN: speeddown, # 快到站，减速
    }
    opt[data["state"]]()
    # 接着进行一些共同的操作——维护countDown，更改下一个状态（根据随机，以及到下一个站点的距离
    # countDown大于0，保持当前状态
    if data["countDown"] > 0:
        data["countDown"] -= 1
        return
    #  * countdown为0，更换新状态
    # * 如果距离足够短且下一站为公交站
    if dist < 100 and data["nodeList"][data["nodeAfterIndex"]]["isStation"]: 
        data["state"] = state.SPEEDDOWN
        data["countDown"] = 0
        return
    if (random.random() * 10) < 9.9: # 90%
        data["state"] = state.NORMAL
        data["countDown"] = 30
    else:
        data["state"] = state.MAKINGTROBLE
        data["countDown"] = 5


