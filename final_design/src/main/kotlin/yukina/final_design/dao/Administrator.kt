package yukina.final_design.dao

import org.apache.ibatis.annotations.*

data class Administrator (
    val admin_id : String,
    val admin_passwd : String,
    val admin_name : String
)

@Mapper
interface AdminMapper {
    @Select("""
        SELECT * FROM administrator
        WHERE admin_id = #{admin_id}
        AND admin_passwd = #{admin_passwd}
    """)
    fun getAdmin(@Param("admin_id") admin_id: String, @Param("admin_passwd") admin_passwd: String) : Administrator?;

    @Insert("""
        INSERT INTO administrator(admin_id,admin_passwd,admin_name)
        VALUES(#{admin_id}, #{admin_passwd}, #{admin_name})
    """)
    fun addAdmin(@Param("admin_id") admin_id : String, @Param("admin_passwd") admin_passwd : String, @Param("admin_name") admin_name : String ) : Int
    @Update("""
        UPDATE administrator SET admin_passwd=#{newPasswd}
        WHERE admin_id = #{admin_id}
    """)
    fun changePasswd(@Param("admin_id") admin_id: String, @Param("newPasswd") newPasswd: String)
    @Update("""
        UPDATE administrator SET admin_name=#{admin_name}
        WHERE admin_id = #{admin_id}
    """)
    fun changeName(@Param("admin_id") admin_id: String, @Param("admin_name") admin_name : String)

    @Select("""
        SELECT admin_passwd FROM administrator WHERE admin_id = #{admin_id}
    """)
    fun getToken(@Param("admin_id") admin_id : String) : String
}
