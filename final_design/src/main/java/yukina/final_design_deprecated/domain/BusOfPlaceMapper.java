package yukina.final_design_deprecated.domain;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;
import java.util.List;

@Mapper
public interface BusOfPlaceMapper {
    @Select("CALL getBusOfPlace(#{placeName}, CURRENT_DATE())")
    List<BusOfPlace> getBusOfPlace(@Param("placeName") String placeName);
    @Select("CALL getBusOfPlace(#{placeName}, #{searchTime})")
    List<BusOfPlace> getBusOfPlaceHistory(@Param("placeName") String placeName, @Param("searchTime") Date searchTime);
}
