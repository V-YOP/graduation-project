package yukina.final_design_deprecated.entity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StationMapper {
    @Select("SELECT * FROM station WHERE station_id=#{station_id}")
    List<Pos> getStationByID(@Param("station_id") Integer station_id);
    //TODO
}
