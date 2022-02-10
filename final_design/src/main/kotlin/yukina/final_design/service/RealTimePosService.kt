package yukina.final_design.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.configurationprocessor.json.JSONArray
import org.springframework.stereotype.Service
import yukina.final_design.controller.UploadPosWS
import yukina.final_design.dao.*
import yukina.final_design.util.getDistance
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.sqrt


/**
 * 抽象前后端上传，获取实时公交车信息的功能的模块
 * 应当维护pos和overspeedPos表
 * ~~使用object（单例对象）来维护其所需信息~~Spring对象本身就是单例的……我个傻子
 */
@Service
class RealTimePosService @Autowired constructor(
    val nodeMapper : NodeMapper,
    val nodepathMapper: NodepathMapper,
    val buslineMapper: BuslineMapper,
    val posMapper: PosMapper,
    val overspeedPosMapper: OverspeedPosMapper
){
    companion object {
        private val log = LoggerFactory.getLogger(UploadPosWS::class.java);
    }
    data class ParsedNodepath(
        val adcode : Int,
        val nodepath_id: Int,
        val latlont1: Pair<BigDecimal, BigDecimal>,
        val latlont2: Pair<BigDecimal, BigDecimal>,
        val speed_limit: Int
    )
    val busline_id2ParsedNodepaths: MutableMap<Int, List<ParsedNodepath>> = ConcurrentHashMap()
    val bus_id2Poses: MutableMap<Int, MutableList<Pos>> = ConcurrentHashMap()
    val bus_id2OverspeedPoses : MutableMap<Int, MutableList<OverspeedPos>> = ConcurrentHashMap()
    // 在开始时就直接获取所有node和nodepath，以减少反复查询
    val node_id2Node = let {
        nodeMapper.getAllNode()
            .map { it.node_id to it }
            .toTypedArray()
            .let { ConcurrentHashMap(hashMapOf(*it)) }
    }
    val nodepath_id2Nodepath = let {
        nodepathMapper.getAllNodepaths()
            .map {it.nodepath_id to it}
            .toTypedArray()
            .let { ConcurrentHashMap(hashMapOf(*it)) }
    }
    private fun addBusline(busline_id : Int) =
        buslineMapper.getBuslineById(busline_id).apply {
            JSONArray(nodepath_id_list).let {
                mutableListOf<ParsedNodepath>().apply {
                    for (i in 0 until it.length()) {
                        val nodepath = nodepath_id2Nodepath[it[i] as Int]
                        val (nodepath_id, node1_id, node2_id, speed_limit, adcode) = nodepath!!
                        val node1 = node_id2Node
                        this.add(ParsedNodepath(
                            adcode,
                            nodepath_id,
                            node_id2Node[node1_id]!!.let { it.lat to it.lont },
                            node_id2Node[node2_id]!!.let {it.lat to it.lont},
                            speed_limit
                        ))
                    }
                }.let { busline_id2ParsedNodepaths[busline_id] = it }
            }
        }

    /**
     * bus上线，进行如下操作——
     * 0. 添加公交车—线路信息
     * 1. 修改公交路线的车辆计数
     * 2. 初始化公交车线路信息 如果没有初始化的话
     * 3. 初始化公交车历史位置数组，如果没有初始化的话
     */
    /*
     fun busOnline(bus_id : Int, busline_id : Int) {

        bus_id2Busline_id[bus_id] = busline_id
        // 计数
        buslineBusCount.apply {
            if (containsKey(busline_id))
                this[busline_id]!!.incrementAndGet()
            else
                this[busline_id] = AtomicInteger(1)
        }
        // 初始化公交车线路
        busline_id2ParsedNodepaths.apply {
            if (containsKey(busline_id)) // 如果公交车线路已经被初始化了，则无视
                return;
            addBusline(busline_id);
        }
        bus_id2Poses.apply {
            put(bus_id, mutableListOf())
        }
    }
    */
    /**
     * 公交车离线，进行如下操作——
     * 0. 删除公交车—线路信息
     * 1. 修改公交线路车辆计数器
     * 2. 清空公交车的历史数据信息（考虑到可以假定公交车切换线路是一次离线——上线的过程）
     * // 3. 如果公交线路上已经没有公交车，删除信息 （不删除，删除没有必要）
     */
    /*
    fun busOffline(bus_id : Int) {

            bus_id2Busline_id[bus_id]?.let { busline_id: Int ->
                bus_id2Busline_id.remove(bus_id);
                buslineBusCount[busline_id]!!.decrementAndGet()
                bus_id2Poses.remove(bus_id);
            }
    }
    */

    /**
     * 公交车上传信息时调用。这个方法调用时公交车和线路应当是已经被初始化过的。
     * 这个方法的任务是：
     * 1. 上传位置信息到数据库
     * 2. 缓存位置信息并对缓存信息进行维护（即限定其长度） // 默认缓存前三分钟的路径
     * 3. 判定是否超速，如果超速则插入数据到超速表中
     */
    fun updatePos(bus_id : Int, line_id : Int, lat : Double, lont : Double, speed: Int, timestamp : Timestamp) {
        val previousSpeed = bus_id2Poses[bus_id]?.last()?.speed
        val A = previousSpeed?.let {speed-it} ?: 0
        posMapper.addPos(bus_id, speed, BigDecimal(lat), BigDecimal(lont), timestamp)
        if (bus_id2Poses[bus_id] == null)
            bus_id2Poses[bus_id] = mutableListOf()
        if (bus_id2OverspeedPoses[bus_id] == null) {
            bus_id2OverspeedPoses[bus_id] = mutableListOf()
        }
        bus_id2Poses[bus_id]!!.apply {
            add(Pos(bus_id, speed, BigDecimal(lat),BigDecimal(lont), timestamp))
            if (size > 120)
                bus_id2Poses[bus_id] = subList(50, size)
        }
        if (busline_id2ParsedNodepaths[line_id] == null)
            addBusline(line_id);
        // TODO : 这里如果有需要的话要进行优化，保存公交车当前所在路径之类的
        busline_id2ParsedNodepaths[line_id]!!
            .find {(_, _, p1, p2, _)->pointOnSegment(BigDecimal(lat) to BigDecimal(lont), p1, p2)}
            // 有很小的概率找不到路径，比这大得多但仍旧很小的概率找到多条路径，应该是数据的问题
            // also可以认为不利用返回值时所使用的let
            ?.also {
                // 判断是否超速，如果超速，则上传到表中
                if (speed <= it.speed_limit)  return;
                overspeedPosMapper.addOverspeedPoses(
                    bus_id,
                    speed,
                    A,
                    it.speed_limit,
                    it.nodepath_id,
                    it.adcode,
                    line_id,
                    BigDecimal(lat),
                    BigDecimal(lont),
                    timestamp)
                // 维护历史的超速信息
                bus_id2OverspeedPoses[bus_id]!!.apply {
                    add(OverspeedPos(bus_id,speed,A,it.speed_limit,it.nodepath_id,it.adcode, line_id,BigDecimal(lat), BigDecimal(lont),timestamp))
                    if (size > 600) {
                        bus_id2OverspeedPoses[bus_id] = subList(50, size)
                    }
                }
            }
    }


    /**
     * 检查点是否存在于线段上面
     * 判断方法是检查点到该线段的垂足是否在线段上，若在，则看距离是否够小。
     * 这种判断法准确率应当足以满足需要
     * 这好像是一道线代题...但是谁还记得啊！
     * 两点求直线的一般方程可以归结为这个式子——
     * (y2-y1)x+(x1-x2)y+x2y1-x1y2=0
     *    a   x + b    y +   c    =0
     *
     *    TODO: 没必要这么干，只需要检查该点到两个端点距离之和是否接近两个端点距离即可！但是这个实现容易出问题
     *    再增加一个条件——
     */
    private fun pointOnSegment(point : Pair<BigDecimal, BigDecimal>,
                       p1 : Pair<BigDecimal, BigDecimal>,
                       p2 : Pair<BigDecimal, BigDecimal>) : Boolean{
        /*
        // 这个实现似乎总是容易出问题
        val (lat, lont) = point
        val (lat1, lont1) = p1
        val (lat2, lont2) = p2
        val distToP1 = getDistance(lat, lont, lat1, lont1)
        val distToP2 = getDistance(lat, lont, lat2, lont2)
        val distP1ToP2 = getDistance(lat1, lont1, lat2, lont2)
        return abs(distToP1 + distToP2 - distP1ToP2) < 5
         */
            // x为经度，y为纬度
        val (x, y) = point // 当前位置
        val (x0, y0) = getPedal(point, p1, p2) // 垂足
        val (x1, y1) = p1 // 线段上第一个点
        val (x2, y2) = p2 // 线段上第二个点
        // 获取到直线的距离
        val dist = getDistance(x,y,x0,y0) // 点到直线的距离
        val diff = BigDecimal(0.000001) // 允许垂足位置存在一些误差
        // 当距离小于5米，且垂足在线段上时则为真
        return dist < 5 && x0 >= minOf(x1, x2)-diff && x0 <= maxOf(x1, x2)+diff
            && y0 >= minOf(y1, y2)-diff && y0 <= maxOf(y1, y2)+diff
    }

    /**
     * 获取点到直线<p1,p2>的垂足
     */
    private fun getPedal(point : Pair<BigDecimal, BigDecimal>,
                         p1 : Pair<BigDecimal, BigDecimal>,
                         p2 : Pair<BigDecimal, BigDecimal>): Pair<BigDecimal, BigDecimal> {
        val (x, y) = point
        val (x1, y1) = p1
        val (x2, y2) = p2
        val a = y2 - y1
        val b = x1 - x2
        val c = (x2 * y1) - (x1 * y2)
        val dx = x1 - x2
        val dy = y1 - y2

        val u = ((x - x1) * (x1 - x2) + (y - y1) * (y1 - y2)) / (dx * dx + dy * dy)
        return (x1 + u * dx) to (y1 + u * dy)
    }

    fun collectResult(busIdList : List<Int>) =
        bus_id2Poses
            .filterKeys (busIdList::contains)
            .values
            .reduceOrNull { a, b -> (a + b) as MutableList<Pos>} to
        bus_id2OverspeedPoses
            .filterKeys (busIdList::contains)
            .values
            .reduceOrNull { a, b -> (a + b) as MutableList<OverspeedPos>}

    fun collectRealtimeResult(busIdList: List<Int>) =
        bus_id2Poses
            .filterKeys (busIdList::contains)
            .values
            .map { it.takeLast(2) } // 获取尾端
            .reduceOrNull { a, b -> (a + b) as MutableList<Pos>} to
        bus_id2OverspeedPoses
            .filterKeys (busIdList::contains)
            .values
            .map { it.takeLast(2) } // 获取尾端
            .reduceOrNull { a, b -> (a + b) as MutableList<OverspeedPos>}
}