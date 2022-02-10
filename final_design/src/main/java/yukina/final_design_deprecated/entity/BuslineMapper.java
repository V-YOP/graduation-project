package yukina.final_design_deprecated.entity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BuslineMapper {
    @Select("SELECT * FROM busline WHERE line_id=#{line_id}")
    List<Busline> getBuslinesByID(@Param("line_id") Integer line_id);
    //TODO
}
