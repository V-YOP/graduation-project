package yukina.final_design.dao

import org.apache.ibatis.annotations.*
import java.sql.Date

data class Nodepath(
    val nodepath_id : Int,
    val node1_id : Int,
    val node2_id : Int,
    val speed_limit : Int,
    val adcode : Int,
    val street_name: String
)

@Mapper
interface NodepathMapper {
    @Select("""
        SELECT * FROM nodepath
        WHERE nodepath_id = #{nodepath_id}
        AND CURRENT_DATE() BETWEEN start_time AND end_time
    """)
    fun getNodepathById(@Param("nodepath_id") nodepath_id: Int) : Nodepath;
    @Select("""
        SELECT * FROM nodepath
        WHERE CURRENT_DATE() BETWEEN start_time AND end_time
    """)
    fun getAllNodepaths() : List<Nodepath>;
    @Select("""
        SELECT * FROM nodepath
        WHERE adcode = #{adcode}
        AND CURRENT_DATE() BETWEEN start_time AND end_time
    """)
    fun getNodepathsByAdcode(@Param("adcode") adcode: Int) : List<Nodepath>
    @Select("""
        SELECT * FROM nodepath
        WHERE adcode = #{adcode}
        AND #{date} BETWEEN start_time AND end_time
    """)
    fun getNodepathsByAdcodeHistory(@Param("adcode") adcode: Int, @Param("date") date : Date) : List<Nodepath>
    @Select("""
        SELECT MAX(nodepath_id) FROM nodepath
    """)
    fun getMaxNodepathId():Int


    @Insert("""
        INSERT INTO nodepath(nodepath_id, node1_id, node2_id,speed_limit,adcode,street_name)
        VALUES (#{nodepath_id}, #{node1_id}, #{node2_id}, #{speed_limit}, #{adcode}, #{street_name})
    """)
    fun addNodepath(
        @Param("nodepath_id") nodepath_id: Int,
        @Param("node1_id") node1_id: Int,
        @Param("node2_id") bode2_id : Int,
        @Param("speed_limit") speed_limit: Int,
        @Param("adcode") adcode: Int,
        @Param("street_name") street_name: String
    )
    @Update("""
        CALL updateNodepath(#{nodepath_id}, #{node1_id}, #{node2_id}, #{speed_limit}, #{adcode}, #{street_name})
    """)
    fun updateNodepath(
        @Param("nodepath_id") nodepath_id: Int,
        @Param("node1_id") node1_id: Int,
        @Param("node2_id") bode2_id : Int,
        @Param("speed_limit") speed_limit: Int,
        @Param("adcode") adcode: Int,
        @Param("street_name") street_name: String
    )
    @Delete("""
        call deleteNodepath(#{nodepath_id})
    """)
    fun deleteNodepath(
        @Param("nodepath_id") nodepath_id: Int
    )
}
