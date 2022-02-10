explain select * from bus, bus_of_place, place
where bus.bus_id = bus_of_place.bus_id and bus_of_place.adcode = place.adcode
