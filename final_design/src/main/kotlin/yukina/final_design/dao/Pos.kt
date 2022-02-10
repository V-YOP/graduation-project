package yukina.final_design.dao

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import java.math.BigDecimal
import java.sql.Timestamp

data class Pos(
    val bus_id: Int,
    val speed: Int,
    val lat: BigDecimal,
    val lont: BigDecimal,
    val update_time: Timestamp = Timestamp(0)
)

data class OverspeedPos (
    val bus_id : Int,
    val speed : Int,
    val A : Int,
    val speed_limit: Int,
    val nodepath_id : Int,
    val adcode : Int,
    val line_id : Int,
    val lat : BigDecimal,
    val lont : BigDecimal,
    val update_time : Timestamp
)

@Mapper
interface PosMapper {
    @Select("""
        <script>
        SELECT * FROM pos WHERE bus_id IN
        <foreach item='bus_ids' collection='array' open='(' separator=',' close=')'>
            #{bus_ids}
        </foreach>
        AND update_time BETWEEN #{start_time} AND #{end_time}
        </script>
    """
    )
    fun getPosesByBusIDsBetween(
        bus_ids: Array<Int>,
        @Param("start_time") start_time: Timestamp,
        @Param("end_time") end_time: Timestamp
    ): List<Pos>

    @Select("""
        SELECT * FROM pos
    """)
    fun getAllPos(): List<Pos>

    @Insert("REPLACE INTO pos(bus_id,speed,lat,lont, update_time) VALUES (#{bus_id},#{speed},#{lat},#{lont}, #{update_time})")
    fun addPos(
        @Param("bus_id") bus_id: Int,
        @Param("speed") speed: Int,
        @Param("lat") lat: BigDecimal,
        @Param("lont") lont: BigDecimal,
        @Param("update_time") update_time: Timestamp
    ): Int
}

@Mapper
interface OverspeedPosMapper {
    @Insert("""
        REPLACE INTO overspeedPos(bus_id, speed,A,speed_limit, nodepath_id, adcode,line_id, lat, lont, update_time)
        VALUES (#{bus_id}, #{speed},#{A},#{speed_limit}, #{nodepath_id}, #{adcode},#{line_id}, #{lat}, #{lont}, #{update_time})
    """)
    fun addOverspeedPoses(
        @Param("bus_id") bus_id: Int,
        @Param("speed") speed: Int,
        @Param("A") A : Int,
        @Param("speed_limit") speed_limit : Int,
        @Param("nodepath_id") nodepath_id: Int,
        @Param("adcode") adcode : Int,
        @Param("line_id") line_id : Int,
        @Param("lat") lat: BigDecimal,
        @Param("lont") lont: BigDecimal,
        @Param("update_time") update_time: Timestamp
    ) : Int

    @Select("""
        <script>
        SELECT * FROM overspeedPos WHERE bus_id IN
        <foreach item='bus_ids' collection='array' open='(' separator=',' close=')'>
            #{bus_ids}
        </foreach>
        AND update_time BETWEEN #{start_time} AND #{end_time}
        </script>
    """
    )
    fun getOverspeedPosesByBusIDsBetween(
        bus_ids: Array<Int>,
        @Param("start_time") start_time: Timestamp,
        @Param("end_time") end_time: Timestamp
    ): List<OverspeedPos>
    @Select("""
        SELECT * FROM overspeedPos WHERE adcode = #{adcode}
        AND update_time BETWEEN #{start_time} AND #{end_time}
    """)
    fun getOverspeedPosesByAdcodeBetween(
        @Param("adcode") adcode : Int,
        @Param("start_time") start_time: Timestamp,
        @Param("end_time") end_time: Timestamp
    ): List<OverspeedPos>
    @Select("""
        SELECT * FROM overspeedPos WHERE adcode = #{adcode}
        AND update_time BETWEEN #{start_time} AND #{end_time}
        ORDER BY rand() LIMIT 500000
    """)
    fun randomGetOverspeedPosesByAdcodeBetween(
        @Param("adcode") adcode : Int,
        @Param("start_time") start_time: Timestamp,
        @Param("end_time") end_time: Timestamp
    ): List<OverspeedPos>
    @Select("""
        SELECT COUNT(*) FROM overspeedPos WHERE adcode = #{adcode}
        AND update_time BETWEEN #{start_time} AND #{end_time}
    """)
    fun getOverspeedPosCountByAdcodeBetween(
        @Param("adcode") adcode : Int,
        @Param("start_time") start_time: Timestamp,
        @Param("end_time") end_time: Timestamp
    ): Int

    @Select("""
        SELECT * FROM overspeedPos WHERE adcode = #{adcode}
    """)
    fun getOverspeedPosesByAdcode(
        @Param("adcode") adcode : Int
    ): List<OverspeedPos>

    @Select("""
        SELECT * FROM overspeedPos WHERE adcode = #{adcode}
        ORDER BY rand() LIMIT #{n}
    """)
    fun randomGetOverspeedPosesByAdcode(
        @Param("adcode") adcode : Int,
        @Param("n") n : Int,
    ): List<OverspeedPos>

    @Select("""
        SELECT * FROM overspeedPos
    """)
    fun getAll(): List<OverspeedPos>
}