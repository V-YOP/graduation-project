package yukina.final_design.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import yukina.final_design.dao.*
import java.sql.Date
import java.sql.Timestamp


@Service
class InfoGetterService @Autowired constructor(
    val busMapper: BusMapper,
    val buslineMapper: BuslineMapper,
    val nodeMapper: NodeMapper,
    val nodepathMapper: NodepathMapper,
    val overspeedPosMapper: OverspeedPosMapper
) {
    /**
     * 前端所使用的数据集合
     */
    data class AllInfo (
        val buses : List<Bus>,
        val buslines : List<Busline>,
        val nodes : List<Node>,
        val nodepaths : List<Nodepath>
    )
    fun getAllInfoByAdcode(adcode : Int): AllInfo =
        AllInfo(busMapper.getBusesByAdcode(adcode),
            buslineMapper.getBuslinesByAdcode(adcode),
            nodeMapper.getNodesByAdcode(adcode),
            nodepathMapper.getNodepathsByAdcode(adcode))

    fun getAllInfosByAdcodes(adcodes : List<Int>) : List<AllInfo> =
        adcodes.map (::getAllInfoByAdcode)

    fun getAllInfoByAdcode(adcode : Int, date : Date) : AllInfo =
        AllInfo(
            busMapper.getBusesByAdcodeHistory(adcode, date),
            buslineMapper.getBuslinesByAdcodeHistory(adcode, date),
            nodeMapper.getNodesByAdcodeHistory(adcode, date),
            nodepathMapper.getNodepathsByAdcodeHistory(adcode, date)
        )

    fun getBusListByAdcode(adcode : Int) : List<Int> =
        busMapper.getBusesByAdcode(adcode).map { it->it.bus_id }

    fun getBusListMapByAdcodes(adcodes : List<Int>) : Map<Int,List<Int>> =
        adcodes
            .map {
                it to this.getBusListByAdcode(it)
            }.let {
                hashMapOf(*it.toTypedArray())
            }
    fun getBuslineListByAdcode(adcode : Int) : List<Int> =
        buslineMapper.getBuslinesByAdcode(adcode).map { it->it.line_id }
    fun getBuslineListMapByAdcodes(adcodes : List<Int>) : Map<Int,List<Int>> =
        adcodes
            .map {
                it to this.getBuslineListByAdcode(it)
            }.let {
                hashMapOf(*it.toTypedArray())
            }

    fun getoverspeedPosesByIdsBetween(busIdList : List<Int>, startTime : Timestamp, endTime : Timestamp) : List<OverspeedPos> =
        overspeedPosMapper.getOverspeedPosesByBusIDsBetween(busIdList.toTypedArray(),startTime,endTime)
    fun getoverspeedPosesByAdcodeBetween( adcode : Int,startTime : Timestamp, endTime : Timestamp) : List<OverspeedPos> =
        overspeedPosMapper.getOverspeedPosesByAdcodeBetween(adcode,startTime,endTime)
    fun getRandomOverspeedPosesByAdcodeBetween( adcode : Int,startTime : Timestamp, endTime : Timestamp) : Pair<List<OverspeedPos>,Double> {
        val overspeedPoses = overspeedPosMapper.randomGetOverspeedPosesByAdcodeBetween(adcode,startTime,endTime)
        val fullNum = overspeedPosMapper.getOverspeedPosCountByAdcodeBetween(adcode,startTime,endTime)
        val scope = fullNum.toDouble() / overspeedPoses.size
        return overspeedPoses to scope
    }





}
