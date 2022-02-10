package yukina.final_design.dao

import org.apache.ibatis.annotations.*
import java.sql.Date
import java.sql.Timestamp

data class Bus(
    val bus_id : Int,
    val bus_number: String,
    val bus_type: String
)

data class BusOfPlace (
    val ID : Int,
    val adcode : Int,
    val bus_id: Int
)

@Mapper
interface BusMapper {
    @Select("""
            SELECT bus_id, bus_number, bus_type
            FROM bus
            WHERE bus_id=#{bus_id}
            AND CURRENT_DATE() BETWEEN start_time AND end_time
        """)
    fun getBusById(@Param("bus_id") bus_id: Int) : Bus

    @Select("""
            SELECT bus_id, bus_number, bus_type
            FROM bus
            WHERE bus_id=#{bus_id}
            AND #{date} BETWEEN start_time AND end_time
        """)
    fun getBusByIdHistory(@Param("bus_id") bus_id: Int, @Param("date") date: Date) : Bus;
    @Select("""
        SELECT bus.bus_id AS bus_id, 
        bus.bus_number AS bus_number, 
        bus.bus_type AS bus_type
        FROM bus, bus_of_place
        WHERE bus.bus_id = bus_of_place.bus_id
        AND bus_of_place.adcode = #{adcode}
        AND CURRENT_DATE() BETWEEN bus.start_time AND bus.end_time
        AND CURRENT_DATE() BETWEEN bus_of_place.start_time AND bus_of_place.end_time
    """)
    fun getBusesByAdcode(@Param("adcode") adcode: Int) : List<Bus>

    @Select("""
        SELECT bus.bus_id AS bus_id, 
        bus.bus_number AS bus_number, 
        bus.bus_type AS bus_type
        FROM bus, bus_of_place
        WHERE bus.bus_id = bus_of_place.bus_id
        AND bus_of_place.adcode = #{adcode}
        AND #{date} BETWEEN bus.start_time AND bus.end_time
        AND #{date} BETWEEN bus_of_place.start_time AND bus_of_place.end_time
    """)
    fun getBusesByAdcodeHistory(@Param("adcode") adcode: Int, @Param("date") date: Date) : List<Bus>
    @Select("""
        SELECT MAX(bus_id) FROM bus
    """)
    fun getMaxBusId() : Int

    @Insert("""
        INSERT INTO bus(bus_id, bus_number, bus_type)
        VALUES (#{bus_id}, #{bus_number}, #{bus_type})
    """)
    fun addBus(@Param("bus_id") bus_id: Int,
               @Param("bus_number") bus_number: String,
               @Param("bus_type") bus_type: String)
    @Update("""
        CALL updateBus(#{bus_id}, #{bus_number}, #{bus_type})
    """)
    fun updateBus(@Param("bus_id") bus_id: Int,
                  @Param("bus_number") bus_number: String,
                  @Param("bus_type") bus_type: String)
    @Delete("""
        CALL deleteBus(#{bus_id})
    """)
    fun deleteBus(@Param("bus_id") bus_id: Int)
}
