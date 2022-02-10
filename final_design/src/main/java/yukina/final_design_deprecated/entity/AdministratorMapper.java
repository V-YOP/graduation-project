package yukina.final_design_deprecated.entity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdministratorMapper {
    @Select("SELECT * FROM administrator WHERE admin_id=#{admin_id} AND admin_passwd=#{admin_passwd}")
    Administrator getAdmin(@Param("admin_id") String admin_id, @Param("admin_passwd") String admin_passwd);
}
