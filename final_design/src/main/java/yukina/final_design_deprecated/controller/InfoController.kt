package yukina.final_design_deprecated.controller

import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import yukina.final_design_deprecated.entity.PlaceMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.configurationprocessor.json.JSONException
import yukina.final_design_deprecated.domain.BusOfPlaceMapper
import yukina.final_design_deprecated.dao.BusOfPlaceDao
import org.springframework.web.bind.annotation.RequestParam
import yukina.final_design_deprecated.domain.BusAndAllNode
import yukina.final_design_deprecated.entity.Pos
import java.text.SimpleDateFormat
import kotlin.Throws


@RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping(value = ["/api"], method = [RequestMethod.GET])
class InfoController {
    @Autowired
    private var placeMapper: PlaceMapper? = null
    @Autowired
    private var buslineMapper: BusOfPlaceMapper? = null
    @Autowired
    private val busOfPlaceDao: BusOfPlaceDao? = null

    @RequestMapping("getAllPlace")
    fun allPlace() : Any = placeMapper!!.allPlace

    @RequestMapping("getBusAndAllNodeInfo")
    fun getBusAndAllNodeInfo(@RequestParam("place_name") place_name: String?): List<BusAndAllNode> {
        val res = busOfPlaceDao!!.getBusAndNodeByPlaceName(place_name);
        return if (place_name is String)
            res
        else emptyList()
    }
    @RequestMapping("getOldPosByBusIDs")
    @Throws(JSONException::class)
    fun getOldPosByBusIDs(@RequestParam("busIDList") busIDList: String?): List<Pos> =
        busOfPlaceDao!!.getOldPosByID(busIDList)

    @RequestMapping("getBusAndAllNodeInfoHistory")
    @Throws(JSONException::class)
    fun getBusAndAllNodeInfoHistory(@RequestParam("place_name") place_name: String?, @RequestParam("query_time") query_time : String?) : List<BusAndAllNode> {
        val date = java.sql.Date(SimpleDateFormat("yyyy-MM-dd").parse(query_time).time)
        return busOfPlaceDao!!.getBusAndNodeByPlaceNameHistory(place_name, date)
    }

    @RequestMapping("getPosesByIDsBetween")
    fun getPosesByIDsBetween (
        @RequestParam("busIdList") busIdList: String?,
        @RequestParam("start_time") start_time : String?,
        @RequestParam("end_time") end_time : String?)
    : List<Pos> {
        fun parse (date: String) : java.sql.Date{
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return java.sql.Date(formatter.parse(date).time);
        }
        return busOfPlaceDao!!.getPosesByIDsBetween(busIdList, parse(start_time!!), parse(end_time!!));
    }
}
