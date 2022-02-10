package yukina.final_design.dao

import org.apache.ibatis.annotations.*
import java.math.BigDecimal

data class Place(
    val adcode : Int,
    val place_name: String,
    val lat : BigDecimal,
    val lont : BigDecimal
)

@Mapper
interface PlaceMapper {
    @Select("""
        SELECT * FROM place
        AND CURRENT_DATE() BETWEEN start_time AND end_time
    """)
    fun getAllPlace() : List<Place>

    @Insert("""
        INSERT INTO place(adcode, place_name, lat, lont)
        VALUES (#{adcode}, #{place_name}, #{lat}, #{lont})
    """)
    fun insertPlace(
        @Param("adcode") adcode: Int,
        @Param("place_name") place_name: String,
        @Param("lat") lat: BigDecimal,
        @Param("lont") lont: BigDecimal
    )
    @Update("""
        CALL updatePlace(#{adcode}, #{lat}, #{lont}, #{place_name})
    """)
    fun updatePlace(
        @Param("adcode") adcode: Int,
        @Param("place_name") place_name: String,
        @Param("lat") lat: BigDecimal,
        @Param("lont") lont: BigDecimal
    )
    @Delete("""
        CALL deletePlace(#{adcode})
    """)
    fun deletePlace(@Param("adcode") adcode: Int)

    @Delete("""
        CALL removeBusFromPlace(#{bus_id}, #{adcode})
    """)
    fun removeBusFromPlace(
        @Param("bus_id") bus_id : Int,
        @Param("adcode") adcode : Int
    )
    @Insert("""
        CALL addBusFromPlace(#{bus_id}, #{adcode})
    """)
    fun addBusFromPlace(
        @Param("bus_id") bus_id : Int,
        @Param("adcode") adcode : Int
    )

    @Insert("""
        CALL addLineFromPlace(#{line_id}, #{adcode})
    """)
    fun addLineFromPlace(
        @Param("line_id") line_id : Int,
        @Param("adcode") adcode : Int
    )
    @Delete("""
        CALL removeLineFromPlace(#{line_id}, #{adcode})
    """)
    fun removeLineFromPlace(
        @Param("line_id") line_id : Int,
        @Param("adcode") adcode : Int
    )
}
