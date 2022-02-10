package yukina.final_design_deprecated.entity

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

@Mapper
interface BusMapper {
    @Select("SELECT * FROM bus WHERE bus_id=#{bus_id}")
    fun getBusesByID(@Param("bus_id") bus_id: Int?): List<Bus?>? //TODO
}