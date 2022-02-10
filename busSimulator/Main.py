import json
from AnotherInfoGetter import getAllInfo
from WSClient import WSClient
import math
import time
from BusHandler import Handler
import random  
# 这个数量太多了（9000多辆），筛选一些
allBus = getAllInfo() # 一共9500（200条线路*19辆）辆公交车
random.shuffle(allBus)
allBus = allBus[:200] # 筛选随机500辆
def main():
    for bus in allBus:
        # time.sleep(random.randint(1,1000)/100000)
        client = WSClient("ws://localhost:8080/ws/uploadPos", Handler(bus))
        client.run()

if __name__=="__main__":
    main()

