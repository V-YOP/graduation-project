use yukinaDB;
set @zjk_lat = 114.877733;
set @zjk_lont = 40.77418;
set @bj_lat = 116.397128;
set @bj_lont = 39.916527;
set @tj_lat = 117.185444;
set @tj_lont = 39.129148;
insert into place(place_name, lat, lont)
values 
("河北省-张家口市-桥东区", @zjk_lat, @zjk_lont),
("北京市-北京市-东城区", @bj_lat, @bj_lont),
("天津市-天津市-河东区", @tj_lat, @tj_lont);


insert into node(lat, lont)
values 
(@zjk_lat, @zjk_lont),
(@zjk_lat - 0.1, @zjk_lont), -- 0.01基本代表1km，所以这个点为向西移动1km
(@zjk_lat - 0.1, @zjk_lont - 0.1),
(@zjk_lat, @zjk_lont - 0.1),
(@bj_lat, @bj_lont),
(@bj_lat - 0.1, @bj_lont),
(@bj_lat - 0.1, @bj_lont - 0.1),
(@bj_lat, @bj_lont - 0.1),
(@tj_lat, @tj_lont),
(@tj_lat - 0.1, @tj_lont),
(@tj_lat - 0.1, @tj_lont - 0.1),
(@tj_lat, @tj_lont - 0.1);

-- 在左下角那个节点不设置公交车站
insert into station(node_id)
values 
(1),(2),(4),(5),(6),(8),(9),(10),(12);
insert into nodepath
(node1_id,node2_id,speed_limit,direction,street_name)
values
(1,2,20,0,"街1"),
(2,3,40,0,"街2"),
(3,4,30,0,"街3"),
(1,5,60,2,"张家口-北京"),
(5,6,50,0,"街1"),
(6,7,70,0,"街2"),
(7,8,60,0,"街3"),
(5,9,80,2,"北京-天津"),
(9,10,25,0,"街1"),
(10,11,40,0,"街2"),
(11,12,70,0,"街3");

insert into bus(bus_number, bus_type)
values 
("冀G1XXX1", "本田"),
("冀G2XXX1", "本田"),
("冀G3XXX1", "本田"),
("冀G1XXX2", "本田"),
("冀G1XXX3", "本田"),
("京A1XXX4", "本田"),
("京A1XXX5", "本田"),
("京A1XXX6", "本田"),
("津A1XXX7", "大众"),
("津A1XXX8", "大众"),
("津A1XXX9", "大众");

-- TODO
insert into busline (line_name,data_json)
values
("1路",'{"data":[[1,true],[1,true],[2,false],[3,true]]}'),
("张家口——北京",'{"data":[[1,true],[4,true]]}'),
("张家口——北京——天津",'{"data":[[1,true],[4,true],[8,true]]}'),
("1路",'{"data":[[5,true],[5,true],[6,false],[7,true]]}'),
("北京——张家口",'{"data":[[5,true],[4,true]]}'),
("北京——天津",'{"data":[[5,true],[8,true]]}'),
("1路",'{"data":[[9,true],[9,true],[10,false],[11,true]]}'),
("天津——北京",'{"data":[[9,true],[8,true]]}'),
("天津——北京——张家口",'{"data":[[9,true],[8,true],[4,true]]}');

-- 设置9条线路——本城市的，到其他两个城市的
insert into line_place(line_id, place_id)
values 
(1,1),
(2,1),(2, 2), 
(3,1),(3,2),(3,3),
(4,2),
(5,2),(5,1),
(6,2),(6,3),
(7,3),
(8,3),(8,2),
(9,3),(9,2),(9,1);

insert into bus_line(line_id, bus_id)
values (1,1),(1,2),(1,3),
(2,4),(3,5),(4,6),(5,7),(6,8),(7,9),(8,10),(9,11);

insert into administrator(admin_id,admin_name,admin_passwd)
values
("ogura", "ogura", "aaaaaa");


