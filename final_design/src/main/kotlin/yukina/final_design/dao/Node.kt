package yukina.final_design.dao

import org.apache.ibatis.annotations.*
import java.math.BigDecimal
import java.sql.Date

data class Node(
    val node_id: Int,
    val adcode: Int,
    val lat: BigDecimal,
    val lont: BigDecimal,
    val isStation: Boolean
)

@Mapper
interface NodeMapper {
    @Select(
        """
        SELECT * FROM Node
        WHERE node_id = #{node_id}
        AND CURRENT_DATE() BETWEEN start_time AND end_time 
    """
    )
    fun getNodeById(@Param("node_id") node_id: Int): Node;

    @Select(
        """
        SELECT * FROM Node WHERE CURRENT_DATE() BETWEEN start_time AND end_time 
    """
    )
    fun getAllNode(): List<Node>;

    @Select(
        """
        SELECT * FROM Node
        WHERE adcode = #{adcode}
        AND CURRENT_DATE() BETWEEN start_time AND end_time
    """
    )
    fun getNodesByAdcode(@Param("adcode") adcode: Int): List<Node>;

    @Select(
        """
        SELECT * FROM Node
        WHERE adcode = #{adcode}
        AND #{date} BETWEEN start_time AND end_time
    """
    )
    fun getNodesByAdcodeHistory(@Param("adcode") adcode: Int, @Param("date") date: Date): List<Node>;

    @Select(
        """
        SELECT MAX(node_id) FROM node
    """
    )
    fun getMaxNodeId(): Int

    @Insert("""
        INSERT INTO node(node_id, lat,lont,isStation,adcode)
        VALUES (#{node_id}, #{lat}, #{lont}, #{isStation}, #{adcode})
    """)
    fun insertNode(
        @Param("node_id") node_id: Int,
        @Param("lat") lat: BigDecimal,
        @Param("lont") lont: BigDecimal,
        @Param("isStation") isStation: Boolean,
        @Param("adcode") adcode: Int
    )

    @Update("""
        CALL updateNode(#{node_id}, #{lat}, #{lont}, #{isStation}, #{adcode})
    """)
    fun updateNode(
        @Param("node_id") node_id: Int,
        @Param("lat") lat: BigDecimal,
        @Param("lont") lont: BigDecimal,
        @Param("isStation") isStation: Boolean,
        @Param("adcode") adcode: Int
    )

    @Delete("""
        CALL deleteNode(#{node_id})
    """)
    fun deleteNode(@Param("node_id") node_id: Int)
}