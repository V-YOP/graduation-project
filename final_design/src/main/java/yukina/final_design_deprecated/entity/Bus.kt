package yukina.final_design_deprecated.entity

import java.sql.Date

// 测试一下mybatis能否直接使用kotlin的data class
data class Bus (
    val bus_id: Int,
    val bus_number: String,
    val bus_type: String,
    val start_time: Date,
    val end_time: Date
)