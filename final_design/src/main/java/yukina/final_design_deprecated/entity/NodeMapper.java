package yukina.final_design_deprecated.entity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;

@Mapper
public interface NodeMapper {
    @Select("SELECT * FROM node WHERE node_id=#{node_id} AND CURRENT_TIME() BETWEEN start_time AND end_time")
    Node getNodeByID(@Param("node_id") Integer node_id);
    //TODO: history
    @Select("SELECT * FROM node WHERE node_id=#{node_id} AND #{date} BETWEEN start_time AND end_time")
    Node getNodeByIDHistory(@Param("node_id") Integer node_id, @Param("date")Date date);
}
