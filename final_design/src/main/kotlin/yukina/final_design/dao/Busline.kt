package yukina.final_design.dao

import org.apache.ibatis.annotations.*
import java.sql.Date

data class Busline(
    val line_id : Int,
    val line_name  : String,
    val start_node_id : Int,
    val bus_id_list : String, // 其为一个JSON字符串，应当在进一步中进行转换
    val nodepath_id_list : String // 同上
)

@Mapper
interface BuslineMapper {
    @Select("""
        SELECT * FROM busline 
        WHERE line_id = #{line_id}
        AND CURRENT_DATE() BETWEEN start_time AND end_time
    """)
    fun getBuslineById(@Param("line_id") line_id: Int):Busline

    @Select("""
        SELECT busline.line_id as line_id,
        line_name, 
        start_node_id,
        bus_id_list,
        nodepath_id_list
        FROM busline, line_of_place
        WHERE busline.line_id = line_of_place.line_id
        AND line_of_place.adcode = #{adcode}
        AND CURRENT_DATE() BETWEEN line_of_place.start_time AND line_of_place.end_time
        AND CURRENT_DATE() BETWEEN busline.start_time AND busline.end_time
    """)
    fun getBuslinesByAdcode(@Param("adcode") adcode: Int) : List<Busline>

    @Select("""
        SELECT busline.line_id as line_id,
        line_name, 
        start_node_id,
        bus_id_list,
        nodepath_id_list
        FROM busline, line_of_place
        WHERE busline.line_id = line_of_place.line_id
        AND line_of_place.adcode = #{adcode}
        AND #{date} BETWEEN line_of_place.start_time AND line_of_place.end_time
        AND #{date} BETWEEN busline.start_time AND busline.end_time
    """)
    fun getBuslinesByAdcodeHistory(@Param("adcode") adcode: Int, @Param("date") date: Date) : List<Busline>

    @Select("""
        SELECT MAX(line_id) FROM busline
    """)
    fun getMaxBuslineId() : Int

    @Insert("""
        INSERT INTO busline(line_id, line_name, start_node_id,bus_id_list,nodepath_id_list)
        VALUES (#{line_id}, #{line_name}, #{start_node_id}, #{bus_id_list}, #{nodepath_id_list})
        
    """)
    fun addBusline(
        @Param("line_id") line_id: Int,
        @Param("line_name") line_name: String,
        @Param("start_node_id") start_node_id: Int,
        @Param("bus_id_list") bus_id_list: String,
        @Param("nodepath_id_list")  nodepath_id_list: String
    )
    @Update("""
        CALL updateBusline(#{line_id}, #{line_name}, #{start_node_id}, #{bus_id_list}, #{nodepath_id_list})
    """)
    fun updateBusline(
        @Param("line_id") line_id: Int,
        @Param("line_name") line_name: String,
        @Param("start_node_id") start_node_id: Int,
        @Param("bus_id_list") bus_id_list: String,
        @Param("nodepath_id_list")  nodepath_id_list: String
    )
    @Delete("""
        CALL deleteBusline(#{line_id})
    """)
    fun deleteBusline(@Param("line_id") line_id: Int)
}


