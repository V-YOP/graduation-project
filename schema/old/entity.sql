DROP DATABASE IF EXISTS yukinaDB;
CREATE DATABASE yukinaDB;
USE yukinaDB;
-- 对应公交车的表
CREATE TABLE bus (
    bus_id INT AUTO_INCREMENT,
    bus_number VARCHAR(7) NOT NULL UNIQUE, -- 车牌号，长度为7位，形如豫A99999
    bus_type VARCHAR(20) NOT NULL, -- 车型
    -- 拉链表所需表项，前闭后开
    start_time DATE, -- 使用触发器或系统给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (bus_id, end_time)
);
CREATE TRIGGER AddBus
BEFORE INSERT ON bus 
FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

-- 对应各个区域的表
CREATE TABLE place (
    place_id INT AUTO_INCREMENT,
    place_name VARCHAR(30) NOT NULL UNIQUE, -- 这个100……
    lat DECIMAL(9,6) NOT NULL,
    lont DECIMAL(9,6) NOT NULL,
    start_time DATE, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (place_id, end_time),
    INDEX PLACE_NAMEINDEX (place_name)
);
CREATE TRIGGER AddPlace
BEFORE INSERT ON place FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

/*
-- 对应驾驶员的表
CREATE TABLE driver (
    driver_id_card VARCHAR(18) NOT NULL PRIMARY KEY, -- 身份证号
    driver_name VARCHAR(10) NOT NULL,
    start_time DATE, 
    end_time DATE DEFAULT "2999-12-31"
);
CREATE TRIGGER AddDriver
BEFORE INSERT ON driver FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();
*/

-- 对应路段节点的表（将被路段表使用）
-- 节点和路段表
CREATE TABLE node(
    node_id INT AUTO_INCREMENT,
    lat DECIMAL(9,6) NOT NULL,
    lont DECIMAL(9,6) NOT NULL,
    start_time DATE, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (node_id, end_time),
    UNIQUE KEY (lat,lont)
);
CREATE TRIGGER AddNode
BEFORE INSERT ON node FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

-- 对应路段的表
CREATE TABLE nodepath(
    nodepath_id INT AUTO_INCREMENT,
    node1_id INT,
    node2_id INT,
    speed_limit TINYINT NOT NULL, 
    -- 方向，为0为从node1通向node2，为1为从node2通向node1，为2为双向
    direction TINYINT NOT NULL, -- 弃用
    street_name VARCHAR(10) NOT NULL,
    start_time DATE, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (nodepath_id, end_time),
    FOREIGN KEY (node1_id) REFERENCES node(node_id),
    FOREIGN KEY (node2_id) REFERENCES node(node_id),
    INDEX NODEPATH (node1_id, node2_id)
    -- UNIQUE KEY(node1_id, node2_id) -- 由于某些bug……先不设这个
);
CREATE TRIGGER AddNodepath
BEFORE INSERT ON nodepath FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

-- 对应公交车站点的表
CREATE TABLE station (
    station_id INT AUTO_INCREMENT,
    node_id INT,
    start_time DATE, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (station_id, end_time),
    FOREIGN KEY (node_id) REFERENCES node(node_id),
    INDEX NODE (node_id)
);
CREATE TRIGGER AddStation
BEFORE INSERT ON station FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();


-- 对应公交车线路的表
CREATE TABLE busline (
    line_id INT AUTO_INCREMENT,
    line_name VARCHAR(20) NOT NULL,
    /*
        data_map是一个相当复杂的数据。它是json格式的。
        它应当保存途径的路段id，站点id，以这样的格式存储——
        {
            data:[
                [start_node_id, aStation?],
                [nodepath_id, aStation?],
                [nodepath_id, aStation?],
                ...
            ]
        }
        其中，aStation标识该nodepath的终点是一个station！
        ! 一个重大的问题是，对nodepath的进一步查询显然是性能极为低下的！
        TODO: 这个或许需要找办法解决！（也可能不解决……）
        我能想到的唯一的办法是将busline所需全部信息
        全部存储在json里，让系统去在
        信息（station，node，nodepath）更新时维护busline表
        
    */
    data_json JSON NOT NULL, 
    start_time DATE, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (line_id, end_time)
);
CREATE TRIGGER AddBusline
BEFORE INSERT ON busline FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

-- 对应公交车发送信息（当前位置，车辆ID）的表
-- 这个表不需要拉链表
CREATE TABLE pos(
    bus_id INT,
    speed TINYINT NOT NULL, -- 速度——公里每小时（迈），tinyint存储0~255的数字
    lat DECIMAL(9,6) NOT NULL,
    lont DECIMAL(9,6) NOT NULL,
    update_time TIMESTAMP DEFAULT NOW(), -- 上传时间
    PRIMARY KEY (bus_id, update_time),
    FOREIGN KEY (bus_id) REFERENCES bus(bus_id)
);

-- 反正这个表的查询一定是按时间和bus_id来的
CREATE TABLE overspeedPos(
    bus_id INT,
    speed TINYINT NOT NULL, -- 速度——公里每小时（迈），tinyint存储0~255的数字
    lat DECIMAL(9,6) NOT NULL,
    lont DECIMAL(9,6) NOT NULL,
    update_time TIMESTAMP DEFAULT NOW(), -- 上传时间
    PRIMARY KEY (bus_id, update_time),
    FOREIGN KEY (bus_id) REFERENCES bus(bus_id)
);

-- 管理员的表
CREATE TABLE administrator(
    admin_id VARCHAR(32) PRIMARY KEY, -- 用作登录
    admin_passwd VARCHAR(32) NOT NULL, -- 可以不存储明文
    admin_name VARCHAR(32) NOT NULL
);

-- 分割线分割线分割线

-- 公交车线路同公交车的对应的表
-- 一条公交车线路上有多个公交车（这里假设没有一辆公交车跑多个线路的情况
CREATE TABLE bus_line (
    ID INT AUTO_INCREMENT,
    line_id INT,
    bus_id INT UNIQUE,
    start_time DATE, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (ID, end_time),
    FOREIGN KEY(bus_id) REFERENCES bus(bus_id),
    FOREIGN KEY(line_id) REFERENCES busline(line_id),
    INDEX LINE_ (line_id),
    INDEX bus (bus_id)
);
CREATE TRIGGER AddBus_line
BEFORE INSERT ON bus_line FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

-- 公交车线路所处于的位置（一般来说只为同一个位置，但跨区的线路有多个）
-- 与之前的想法不同（通过位置确定所需站点，再通过站点确定线路），选择直接通过位置确定线路。
CREATE TABLE line_place (
    ID INT AUTO_INCREMENT,
    line_id INT,
    place_id INT,
    start_time DATE NOT NULL, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (ID, end_time),
    FOREIGN KEY (place_id) REFERENCES place(place_id),
    FOREIGN KEY (line_id) REFERENCES busline(line_id),
    index line_ (line_id),
    index place (place_id)
);
CREATE TRIGGER AddLine_place
BEFORE INSERT ON line_place FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

-- 公交车的驾驶员，一个驾驶员可能可以驾驶多个公交车，一个公交车可能可以由多个驾驶员驾驶
/*
CREATE TABLE driver_bus (
    ID INT,
    driver_id_card VARCHAR(18),
    bus_id INT,
    start_time DATE NOT NULL, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (ID, end_time),
    FOREIGN KEY (driver_id_card) REFERENCES driver(driver_id_card),
    FOREIGN KEY (bus_id) REFERENCES bus(bus_id)
);
CREATE TRIGGER AddDriver_bus
BEFORE INSERT ON driver_bus FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();
*/


CREATE VIEW bus_info AS 
SELECT 
    place.place_name AS place_name,
    bus_line.bus_id AS bus_id,
    busline.line_id AS line_id,
    busline.data_json AS data_json,
    busline.line_name AS line_name,
    bus_line.start_time AS start_time1,
    bus_line.end_time AS end_time1,
    busline.start_time AS start_time2,
    busline.end_time AS end_time2,
    line_place.start_time AS start_time3,
    line_place.end_time AS end_time3,
    place.start_time AS start_time4,
    place.end_time AS end_time4
FROM bus_line, busline, line_place, place
WHERE bus_line.line_id = busline.line_id AND 
    busline.line_id = line_place.line_id AND 
    line_place.place_id = place.place_id;

CREATE PROCEDURE getBusInfo (IN searchTime DATE)
BEGIN
    SELECT place_name, bus_id, data_json, line_name, line_id FROM bus_info
    WHERE searchTime BETWEEN start_time1 AND end_time1 AND 
    searchTime BETWEEN start_time2 AND end_time2 AND
    searchTime BETWEEN start_time3 AND end_time3 AND
    searchTime BETWEEN start_time4 AND end_time4;
END;


CREATE PROCEDURE getBusOfPlace (IN placeName VARCHAR(30), IN searchTime DATE)
BEGIN
    SELECT place_name, bus_id, data_json, line_name, line_id FROM bus_info
    WHERE place_name = placeName AND
    searchTime BETWEEN start_time1 AND end_time1 AND 
    searchTime BETWEEN start_time2 AND end_time2 AND
    searchTime BETWEEN start_time3 AND end_time3 AND
    searchTime BETWEEN start_time4 AND end_time4;
END;

