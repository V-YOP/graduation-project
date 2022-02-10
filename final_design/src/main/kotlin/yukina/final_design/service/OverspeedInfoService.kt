package yukina.final_design.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import yukina.final_design.dao.OverspeedPos
import yukina.final_design.dao.OverspeedPosMapper
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor


@Service
class OverspeedInfoService @Autowired constructor(
    val overspeedPosMapper: OverspeedPosMapper
){
    val cache = let {
        val allInfo = overspeedPosMapper.getAll()
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        allInfo
            .groupBy { it.adcode }
            .mapValues { (_,v) ->
                v.shuffled().groupBy {
                    formatter.format(it.update_time)
                }
            }
    }

    fun getOverspeedInfo(adcode : Int, dateRange : Pair<String,String>): Pair<List<OverspeedPos>, Double>? {
        if (!cache.containsKey(adcode))
            return null
        fun getDays(startTime: String, endTime: String): List<String> {
            // 返回的日期集合
            val days: MutableList<String> = ArrayList()
            val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
            try {
                val start: Date = dateFormat.parse(startTime)
                val end: Date = dateFormat.parse(endTime)
                val tempStart = Calendar.getInstance()
                tempStart.time = start
                val tempEnd = Calendar.getInstance()
                tempEnd.time = end
                tempEnd.add(Calendar.DATE, +1) // 日期加1(包含结束)
                while (tempStart.before(tempEnd)) {
                    days.add(dateFormat.format(tempStart.time))
                    tempStart.add(Calendar.DAY_OF_YEAR, 1)
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return days
        }
        val days = getDays(dateRange.first,dateRange.second)
        val fullSize = days.sumOf {
            (cache[adcode]!![it]?.size) ?: 0
        }
        val scope = 500000.0 / fullSize
        val res = days.map { date ->
            (cache[adcode]!![date] ?: listOf())
                .let {
                    it.take(floor(it.size * scope).toInt())
                }
        }.flatten()
        return (res to scope)
    }
}