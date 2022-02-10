import json
# 读取json文件


def getAllInfo():
    data = None
    with open('./data.json','r',encoding='utf8')as fp:
        data = json.load(fp)
    return data