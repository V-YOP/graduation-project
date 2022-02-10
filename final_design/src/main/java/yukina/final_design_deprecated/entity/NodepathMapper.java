package yukina.final_design_deprecated.entity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;

@Mapper
public interface NodepathMapper {
    @Select("SELECT * FROM nodepath WHERE nodepath_id=#{nodepath_id} AND CURRENT_TIME() BETWEEN start_time AND end_time")
    Nodepath getNodepathByID(@Param("nodepath_id") Integer nodepath_id);

    //TODO: history
    @Select("SELECT * FROM nodepath WHERE nodepath_id=#{nodepath_id} AND #{date} BETWEEN start_time AND end_time")
    Nodepath getNodepathByIDHistory(@Param("nodepath_id") Integer nodepath_id, @Param("date")Date date);
}
