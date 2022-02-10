package yukina.final_design_deprecated.entity;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TestMapper {
    @Insert("CALL addTestData(#{data})")
    void addTestData(@Param("data") Integer data);
    @Select("CALL getAllTestData()")
    List<Test> getAllTestData();
}
