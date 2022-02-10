package yukina.final_design.controller

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.*
import yukina.final_design.dao.OverspeedPos
import yukina.final_design.util.parseyyyyMMdd2Date
import yukina.final_design.service.InfoGetterService
import yukina.final_design.service.OverspeedInfoService
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date


@RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping(value= ["/api"], method= [RequestMethod.GET])
class InfoController @Autowired constructor(
    val infoGetterService: InfoGetterService,
    val overspeedInfoService: OverspeedInfoService
) {
    /**
     * date是yyyy-MM-dd格式字符串
     */
    @RequestMapping("getInfo")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun justGetAll(@RequestParam("adcode") adcode: Int, @RequestParam("date") date:String?) : InfoGetterService.AllInfo {
        return if (date is String) {
            infoGetterService.getAllInfoByAdcode(adcode, parseyyyyMMdd2Date(date))
        }
        else infoGetterService.getAllInfoByAdcode(adcode)
    }
    @RequestMapping("getInfos")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun anotherOne(@RequestParam("adcodes") adcodeStr: String) : List<InfoGetterService.AllInfo> {
        val adcodes = adcodeStr.split(",").map{it.toInt()}
        return infoGetterService.getAllInfosByAdcodes(adcodes)
    }

    @RequestMapping("getOverspeedInfo")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun getOverspeedInfo(@RequestParam("adcode") adcode: Int, @RequestParam("date") date:String) {
        TODO();
    }
    @RequestMapping("getPosesByIDsBetween")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun getPosesByBusIDsBetween(
        @RequestParam("busIdList") busIdList : String,
        @RequestParam("startTime") startTime : String, // YYYY-MM-DD hh:mm:ss
        @RequestParam("endTime") endTime : String
    ): List<OverspeedPos> {
        return infoGetterService.getoverspeedPosesByIdsBetween(
            busIdList
            .let(JSONObject::parseArray)
            .toJavaList(Int::class.java), Timestamp.valueOf(startTime), Timestamp.valueOf(endTime))
    }
    @RequestMapping("getOverspeedPosesByAdcodeBetween")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun getOverspeedPosesByAdcodeBetween(
        @RequestParam("adcode") adcode : Int,
        @RequestParam("startTime") startTime : String, // YYYY-MM-DD hh:mm:ss
        @RequestParam("endTime") endTime : String
    ): List<OverspeedPos> {
        return infoGetterService.getoverspeedPosesByAdcodeBetween(adcode , Timestamp.valueOf(startTime), Timestamp.valueOf(endTime))
    }
    @RequestMapping("getAdcode2busIdsMap")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun getAdcode2busIdsMap(
        @RequestParam("adcodes") adcodes : String,
    ) = adcodes.split(",").map{
            it.toInt()
        }.let {
            infoGetterService.getBusListMapByAdcodes(it)
        }
    @RequestMapping("getAdcode2buslineIdsMap")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun getAdcode2buslineIdsMap(
        @RequestParam("adcodes") adcodes : String,
    ) = adcodes.split(",").map{
        it.toInt()
    }.let {
        infoGetterService.getBuslineListMapByAdcodes(it)
    }

    // startDate和endDate前闭后闭
    @RequestMapping("getRandomOverspeedPosesByAdcodeBetween")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun getRandomOverspeedPosesByAdcodeBetween(
        @RequestParam("adcode") adcode : Int,
        @RequestParam("startDate") startTime : String, // YYYY-MM-DD
        @RequestParam("endDate") endTime : String
    ): Map<String, Any> {
        val (overspeedPoses,scope) = infoGetterService.getRandomOverspeedPosesByAdcodeBetween(adcode , Timestamp.valueOf("""$startTime 00:00:01"""), Timestamp.valueOf("""$endTime 23:59:59"""))
        return mapOf(
            "overspeedPoses" to overspeedPoses,
            "scope" to scope
        )
    }

    // 测试用，把东西存在内存里
    val cache by lazy {
        infoGetterService.getRandomOverspeedPosesByAdcodeBetween(110101 , Timestamp.valueOf("""2021-05-30 00:00:01"""), Timestamp.valueOf("""2021-06-15 23:59:59"""))
    }
    @RequestMapping("getRandomOverspeedPosesByAdcodeBetweenTest")
    @CrossOrigin(origins = ["*"], maxAge = 3600)
    fun getRandomOverspeedPosesByAdcodeBetweenTest(
        @RequestParam("adcode") adcode : Int,
        @RequestParam("startDate") startTime : String, // YYYY-MM-DD
        @RequestParam("endDate") endTime : String
    ): Map<String, Any> {
        val res = overspeedInfoService.getOverspeedInfo(adcode, startTime to endTime) ?: error("adcode的地区没有找到")
        return mapOf(
            "overspeedPoses" to res.first,
            "scope" to 1 / res.second
        )
    }
}