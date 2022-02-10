DROP DATABASE IF EXISTS yukinaDB;
CREATE DATABASE yukinaDB;
USE yukinaDB;

CREATE TABLE administrator(
    admin_id VARCHAR(32) PRIMARY KEY, -- 用作登录
    admin_passwd VARCHAR(32) NOT NULL, -- 可以不存储明文
    admin_name VARCHAR(32) NOT NULL
);

CREATE TABLE place (
    adcode INT,
    place_name VARCHAR(30) NOT NULL,
    lat DECIMAL(9,6) NOT NULL,
    lont DECIMAL(9,6) NOT NULL,
    start_time DATE, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (adcode,  end_time),
    INDEX place_name(place_name)
);
CREATE TRIGGER AddPlace
BEFORE INSERT ON place FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

CREATE TABLE bus (
    bus_id INT,
    bus_number VARCHAR(7) NOT NULL, -- 车牌号，长度为7位，形如豫A99999
    bus_type VARCHAR(20) NOT NULL, -- 车型
    start_time DATE, -- 使用触发器或系统给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (bus_id,  end_time),
    INDEX (bus_number)
);
CREATE TRIGGER AddBus
BEFORE INSERT ON bus 
FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

CREATE TABLE bus_of_place (
    ID INT AUTO_INCREMENT,
    adcode INT NOT NULL, -- 应当对应所有公交车所经过的节点
    bus_id INT NOT NULL,
    start_time DATE NOT NULL, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (ID, end_time),
    FOREIGN KEY (adcode) REFERENCES place(adcode),
    FOREIGN KEY (bus_id) REFERENCES bus(bus_id),
    index (adcode),
    index (bus_id),
);
CREATE TRIGGER AddBusOfPlace
BEFORE INSERT ON bus_of_place FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

CREATE TABLE node(
    node_id INT,
    lat DECIMAL(9,6) NOT NULL,
    lont DECIMAL(9,6) NOT NULL,
    isStation BIT NOT NULL,
    adcode INT NOT NULL, -- 标识所在地点
    start_time DATE, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (node_id,  end_time),
    FOREIGN KEY (adcode) REFERENCES place(adcode),
    INDEX (adcode)
);
CREATE TRIGGER AddNode
BEFORE INSERT ON node FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

CREATE TABLE nodepath(
    nodepath_id INT,
    node1_id INT,
    node2_id INT,
    speed_limit TINYINT NOT NULL, 
    adcode INT,
    street_name VARCHAR(20) NOT NULL,
    start_time DATE, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (nodepath_id,  end_time),
    FOREIGN KEY (node1_id) REFERENCES node(node_id),
    FOREIGN KEY (node2_id) REFERENCES node(node_id),
    FOREIGN KEY (adcode) REFERENCES place(adcode),
    INDEX (adcode)
);
CREATE TRIGGER AddNodepath
BEFORE INSERT ON nodepath FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

CREATE TABLE busline (
    line_id INT,
    line_name VARCHAR(20) NOT NULL,
    start_node_id INT,
    -- place_list JSON NOT NULL, -- place_list需要被查询……所以不使用JSON，使用另外的表
    bus_id_list JSON NOT NULL, -- 这有点像是一个馊主意……
    nodepath_id_list JSON NOT NULL, -- 这个不需要被解析，直接查询即可
    start_time DATE, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY (line_id,  end_time),
    FOREIGN KEY (start_node_id) REFERENCES node(node_id)
);
CREATE TRIGGER AddBusline
BEFORE INSERT ON busline FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

CREATE TABLE line_of_place (
    ID INT AUTO_INCREMENT,
    adcode INT,
    line_id INT,
    start_time DATE NOT NULL, -- 使用触发器给值
    end_time DATE DEFAULT "2999-12-31",
    PRIMARY KEY(ID, end_time),
    FOREIGN KEY (adcode) REFERENCES place(adcode),
    FOREIGN KEY (line_id) REFERENCES busline(line_id),
    INDEX (adcode),
    INDEX (line_id)
);
CREATE TRIGGER AddLine_place
BEFORE INSERT ON line_of_place FOR EACH ROW
SET NEW.start_time=CURRENT_DATE();

CREATE TABLE pos(
    bus_id INT,
    speed TINYINT NOT NULL, -- 速度——公里每小时（迈），tinyint存储0~255的数字
    lat DECIMAL(9,6) NOT NULL,
    lont DECIMAL(9,6) NOT NULL,
    update_time TIMESTAMP DEFAULT NOW(), -- 上传时间
    PRIMARY KEY (bus_id, update_time),
    FOREIGN KEY (bus_id) REFERENCES bus(bus_id),
    INDEX update_time (update_time)
);

CREATE TABLE overspeedPos(
    bus_id INT,
    speed TINYINT NOT NULL, -- 速度——公里每小时（迈），tinyint存储0~255的数字
    A TINYINT NOT NULL,
    speed_limit TINYINT NOT NULL,
    nodepath_id INT NOT NULL, 
    adcode INT NOT NULL,  -- 为查询方便，同时给定adcode
    line_id INT NOT NULL,
    lat DECIMAL(9,6) NOT NULL,
    lont DECIMAL(9,6) NOT NULL,
    update_time TIMESTAMP DEFAULT NOW(), -- 上传时间
    PRIMARY KEY (bus_id, update_time),
    FOREIGN KEY (bus_id) REFERENCES bus(bus_id),
    FOREIGN KEY (adcode) REFERENCES place(adcode),
    FOREIGN KEY (nodepath_id) REFERENCES nodepath(nodepath_id),
    FOREIGN KEY (line_id) REFERENCES busline(line_id),
    INDEX update_time (update_time),
    INDEX nodepath_id (nodepath_id),
    INDEX adcode (adcode)
);

-- 算累加，也就是说，对每一个时间段，对每一个nodepath，计算其pos从0到100位置（段）处超速的数量
-- 重要的不是超速的数量，而是某个能够衡量该路段超速的程度的值
-- 这个值应当和这个路段中的所有速度值有关，且几个高速度的点的权重显然大于更多数量低速度的点（说明点的数量无关紧要）
-- 速度的平均值？但是速度的最大值显然也是比较重要的……如何获取最合适的权重？
-- 这个路段超速的公交车的数量大概也是比较重要的……
CREATE TABLE another(
    bus_id INT,
    nodepath_id INT NOT NULL, 
    adcode INT NOT NULL,  -- 为查询方便，同时给定adcode
    line_id INT NOT NULL,
    speed JSON NOT NULL, -- 速度的数组
    A JSON NOT NULL, -- 超速度的数组
    time_segment TIMESTAMP NOT NULL, -- 标识时间段的开始，段长度为15分钟
    PRIMARY KEY (nodepath_id, update_time),
    FOREIGN KEY (adcode) REFERENCES place(adcode),
    FOREIGN KEY (nodepath_id) REFERENCES nodepath(nodepath_id)
);


CREATE PROCEDURE updateNode (node_id_ INT, lat_ DECIMAL(9, 6), lont_ DECIMAL(9,6), isStation_ BIT, adcode_ INT)
BEGIN
    -- 删除可能存在的当日的更新
    SET foreign_key_checks = 0;
    DELETE FROM node WHERE 
    node_id = node_id_ 
    AND start_time = CURRENT_DATE() 
    AND end_time = "2999-12-31";

    -- 更改原数据终止时间
    call deleteNode(node_id_);
    -- 插入新数据
    INSERT INTO node(node_id, lat, lont, isStation, adcode)
    VALUES (node_id_, lat_, lont_, isStation_, adcode_);
    SET foreign_key_checks = 1;
END;

CREATE PROCEDURE deleteNode (node_id_ INT)
BEGIN
    -- 更改原数据终止时间
    UPDATE node 
    SET end_time = DATE_ADD(CURRENT_DATE(), INTERVAL -1 day) 
    WHERE node_id = node_id_ AND end_time = "2999-12-31";
END;

CREATE PROCEDURE deletePlace (adcode_ INT)  
BEGIN 
    UPDATE place 
    SET end_time = DATE_ADD(CURRENT_DATE(), INTERVAL -1 day) 
    WHERE adcode = adcode_ AND end_time = "2999-12-31";
END;

CREATE PROCEDURE updatePlace (adcode_ INT,lat_ DECIMAL(9, 6), lont_ DECIMAL(9,6), place_name_ VARCHAR(30) )  
BEGIN 
    SET foreign_key_checks = 0;
    DELETE FROM place WHERE 
    adcode = adcode_ 
    AND start_time = CURRENT_DATE() 
    AND end_time = "2999-12-31";

    -- 更改原数据终止时间
    call deletePlace(adcode_);
    -- 插入新数据
    INSERT INTO place(adcode, lat, lont, place_name)
    VALUES (adcode_, lat_, lont_, place_name_);
    SET foreign_key_checks = 1;
END;

CREATE PROCEDURE deleteBus (bus_id_ INT)  
BEGIN 
    UPDATE bus 
    SET end_time = DATE_ADD(CURRENT_DATE(), INTERVAL -1 day) 
    WHERE bus_id = bus_id_ AND end_time = "2999-12-31";
END;

CREATE PROCEDURE updateBus (bus_id_ INT, bus_number_ VARCHAR(7), bus_type_ VARCHAR(7))  
BEGIN 
    SET foreign_key_checks = 0;
    DELETE FROM bus WHERE 
    bus_id = bus_id_ 
    AND start_time = CURRENT_DATE() 
    AND end_time = "2999-12-31";
    CALL deleteBus(bus_id_);
    INSERT INTO bus(bus_id, bus_number, bus_type) 
    values(bus_id_, bus_number_, bus_type_);
    SET foreign_key_checks = 1;
END;

-- 从bus_of_place表中删除这个关系
CREATE PROCEDURE removeBusFromPlace(bus_id_ INT, adcode_ INT)
BEGIN 
    UPDATE bus_of_place 
    SET end_time = DATE_ADD(CURRENT_DATE(), INTERVAL -1 day) 
    WHERE bus_id = bus_id_ AND adcode = adcode_
    AND end_time = "2999-12-31";
END;
CREATE PROCEDURE addBusFromPlace(bus_id_ INT, adcode_ INT)
BEGIN 
    IF NOT EXISTS(SELECT ID FROM bus_of_place WHERE bus_id = bus_id_ AND adcode = adcode_ AND CURRENT_DATE() BETWEEN start_time AND end_time) THEN
        INSERT INTO bus_of_place(adcode, bus_id) -- 解决可能的重复插入问题
        VALUES (adcode_, bus_id_);
    END IF;
END;

CREATE PROCEDURE deleteNodepath(nodepath_id_ INT)
BEGIN 
    UPDATE nodepath 
    SET end_time = DATE_ADD(CURRENT_DATE(), INTERVAL -1 day) 
    WHERE nodepath_id = nodepath_id_ AND end_time = "2999-12-31";
END;

CREATE PROCEDURE updateNodepath(nodepath_id_ INT, node1_id_ INT, node2_id_ INT, speed_limit_ TINYINT, adcode_ INT, street_name_ VARCHAR(20))
BEGIN 
    SET foreign_key_checks = 0;
    DELETE FROM nodepath WHERE 
    nodepath_id = nodepath_id_ 
    AND start_time = CURRENT_DATE() 
    AND end_time = "2999-12-31";

    -- 更改原数据终止时间
    call deleteNodepath(nodepath_id_);
    -- 插入新数据
    INSERT INTO nodepath(nodepath_id,node1_id,node2_id,speed_limit,adcode,street_name)
    VALUES (nodepath_id_, node1_id_, node2_id_, speed_limit_, adcode_, street_name_);
    SET foreign_key_checks = 1;
END;

CREATE PROCEDURE deleteBusline (line_id_ INT)
BEGIN 
    UPDATE busline 
    SET end_time = DATE_ADD(CURRENT_DATE(), INTERVAL -1 day) 
    WHERE line_id = line_id_ AND end_time = "2999-12-31";
END;

CREATE PROCEDURE updateBusline (line_id_ INT, line_name_ VARCHAR(20), start_node_id_ INT, bus_id_list_ JSON, nodepath_id_list_ JSON)
BEGIN 
    SET foreign_key_checks = 0;
    DELETE FROM busline WHERE 
    line_id = line_id_ 
    AND start_time = CURRENT_DATE() 
    AND end_time = "2999-12-31";

    -- 更改原数据终止时间
    call deleteBusline(line_id_);
    -- 插入新数据
    INSERT INTO busline(line_id,line_name,start_node_id,bus_id_list,nodepath_id_list)
    VALUES (line_id_, line_name_, start_node_id_,bus_id_list_,nodepath_id_list_);
    SET foreign_key_checks = 1;
END;

CREATE PROCEDURE removeLineFromPlace(line_id_ INT, adcode_ INT)
BEGIN 
    UPDATE line_of_place 
    SET end_time = DATE_ADD(CURRENT_DATE(), INTERVAL -1 day) 
    WHERE line_id = line_id_ AND adcode = adcode_
    AND end_time = "2999-12-31";
END;
CREATE PROCEDURE addLineFromPlace(line_id_ INT, adcode_ INT)
BEGIN 
    IF NOT EXISTS(SELECT ID FROM line_of_place WHERE line_id = line_id_ AND adcode = adcode_ AND CURRENT_DATE() BETWEEN start_time AND end_time) THEN
        INSERT INTO line_of_place(adcode, line_id) -- 使用REPLACE以解决可能的重复插入问题
        VALUES (adcode_, line_id_);
    END IF;
END;


