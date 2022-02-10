-- 查询用的PROCEDURE
USE yukinaDB;
CREATE PROCEDURE getBusesOfPlace (IN theTime DATE)
BEGIN
    SELECT 
        place.place_name AS place_name, 
        bus.bus_id AS bus_id, 
        bus.bus_number AS bus_number,
        bus.bus_type AS bus_type 
    FROM bus, place, bus_line, line_place
    WHERE line_place.line_id = bus_line.line_id AND 
        bus_line.bus_id = bus.bus_id AND
        line_place.place_id = place.place_id AND
        theTime BETWEEN bus.start_time AND bus.end_time AND 
        theTime BETWEEN place.start_time AND place.end_time AND
        theTime BETWEEN bus_line.start_time AND bus_line.end_time AND
        theTime BETWEEN line_place.start_time AND line_place.end_time;
END;