package yukina.final_design_deprecated.entity;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Mapper
public interface PosMapper {
    @Select("SELECT * FROM pos WHERE bus_id=#{bus_id}")
    List<Pos> getPosesByBusID(@Param("bus_id") Integer bus_id);
    //TODO
    @Insert("INSERT INTO pos(bus_id, speed,lat,lont) values (#{bus_id},#{speed},#{lat},#{lont})")
    Integer uploadPos(@Param("bus_id")Integer bus_id, @Param("speed") Integer speed, @Param("lat") BigDecimal lat, @Param("lont") BigDecimal lont);

    @Select({
            "<script>" +
            "SELECT * FROM pos WHERE bus_id IN" +
            "<foreach item='bus_ids' collection='array' open='(' separator=',' close=')'>" +
            "#{bus_ids}"+
            "</foreach>" +
            "AND ABS(TIMESTAMPDIFF(second, NOW(), update_time)) &lt; 2" +
            "</script>"
    })
    List<Pos> getPosesByIDsPresent(Integer[] bus_ids);

    @Select({
            "<script>" +
                    "SELECT * FROM pos WHERE bus_id IN" +
                    "<foreach item='bus_ids' collection='array' open='(' separator=',' close=')'>" +
                    "#{bus_ids}"+
                    "</foreach>" +
                    "AND ABS(TIMESTAMPDIFF(second, NOW(), update_time)) &lt; 100" +
                    "</script>"
    })
    List<Pos> getPosesByIDsLong(Integer[] bus_ids);

    @Select({
            "<script>" +
                    "SELECT * FROM pos WHERE bus_id IN" +
                    "<foreach item='bus_ids' collection='array' open='(' separator=',' close=')'>" +
                    "#{bus_ids}"+
                    "</foreach>" +
                    "AND update_time BETWEEN #{start_time} AND #{end_time}" +
                "</script>"
    })
    List<Pos> getPosesByIDsBetween(Integer[] bus_ids, @Param("start_time") Date start_time, @Param("end_time") Date end_time);
}
