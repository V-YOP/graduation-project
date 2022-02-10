package yukina.final_design_deprecated.entity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;
import java.util.List;

@Mapper
public interface PlaceMapper {
    @Select("SELECT * FROM place WHERE place_id=#{place_id}")
    List<Place> getPlacesByID(@Param("place_id") Integer place_id);
    //TODO
    @Select("SELECT * FROM place WHERE CURRENT_TIME() BETWEEN start_time and end_time")
    List<Place> getAllPlace();
    @Select("SELECT * FROM place WHERE #{updateTime} BETWEEN start_time and end_time")
    List<Place> getAllPlaceHistory(@Param("updateTime") Date date);
    @Select("SELECT * FROM place WHERE place_name=#{place_name} AND CURRENT_TIME() BETWEEN start_time and end_time")
    Place getPlaceByName(@Param("place_name") String place_name);

    @Select("SELECT * FROM place WHERE place_name=#{place_name} AND #{updateTime} BETWEEN start_time and end_time")
    Place getPlaceByNameHistory(@Param("place_name") String place_name, @Param("updateTime") Date date);
}
