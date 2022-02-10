package yukina.final_design.util

import java.text.SimpleDateFormat

fun parseyyyyMMdd2Date(str: String) = java.sql.Date(SimpleDateFormat("yyyy-MM-dd").parse(str).time);
