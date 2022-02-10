package yukina.final_design.service

import com.alibaba.fastjson.JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.DigestUtils
import org.springframework.util.MultiValueMap
import yukina.final_design.dao.*
import java.math.BigDecimal
import kotlin.math.abs


// 用于进行管理操作的Service
@Service
class AdminService @Autowired constructor(
    val adminMapper: AdminMapper,
    val busMapper: BusMapper,
    val buslineMapper: BuslineMapper,
    val nodeMapper: NodeMapper,
    val nodepathMapper: NodepathMapper,
    val placeMapper: PlaceMapper
) {
    val salt = "%#Fhuy4^$@#687g]_TC" // 盐
    fun addSalt(str : String) = DigestUtils.md5DigestAsHex((str + salt).encodeToByteArray())
    fun login(id: String, passwd: String): Pair<Administrator?, String> {
        val realPasswd = addSalt(passwd)
        return adminMapper.getAdmin(id, realPasswd) to realPasswd;
    }

    // 校验，更改密码，如果成功则返回新token，否则返回null
    fun changePasswd(id : String, oldPasswd : String, newPasswd : String) : String? {
        adminMapper.getAdmin(id, addSalt(oldPasswd))?.let { _ ->
            val newToken = addSalt(newPasswd)
            adminMapper.changePasswd(id, newToken)
            return newToken
        }
        return null
    }
    fun CHANGEPASSWD(passwd : String) {
        adminMapper.changePasswd("yukina", addSalt(passwd))
    }
    fun changeName(id : String, token : String, newName : String) : String? {
        adminMapper.getAdmin(id, token)?.let { _ ->
            adminMapper.changeName(id, newName)
            return "success"
        }
        return null
    }
    fun getToken(id : String): String {
        return adminMapper.getToken(id)
    }

    /*
    fun tmpAddAdmin (id : String, passwd: String, name:String){
        val realPasswd = DigestUtils.md5DigestAsHex((passwd+salt).encodeToByteArray())
        println(realPasswd);
        adminMapper.addAdmin(id, realPasswd, name);
    }
    */
    data class Option(
        val target: String,
        val type: String,
        val data: MutableMap<String, Any>
    )

    // 控制器执行的方法，应当对各种ID进行转换，排序，分发，这里直接用synchronized，从而避免线程不安全
    @Transactional
    fun execute(options: List<Option>) = synchronized(this) {
        val key2Value = mapOf(
            "INSERT" to 100,
            "UPDATE" to 200,
            "DELETE" to -300, // 弃用，现只用于检测值是否合法
            "PLACE" to 1,
            "BUS" to 2,
            "NODE" to 3,
            "NODEPATH" to 4,
            "BUSLINE" to 5,
            "BUS_OF_PLACE" to 6,
            "BUSLINE_OF_PLACE" to 7
        )
        val maxNodeId = nodeMapper.getMaxNodeId();
        val maxNodepathId = nodepathMapper.getMaxNodepathId()
        val maxBusId = busMapper.getMaxBusId()
        val parse = mapOf(
            "BUS" to ("bus_id" to maxBusId),
            "BUS_OF_PLACE" to ("bus_id" to busMapper.getMaxBusId()),
            "BUSLINE" to ("line_id" to buslineMapper.getMaxBuslineId()),
            "BUSLINE_OF_PLACE" to ("line_id" to buslineMapper.getMaxBuslineId()),
            "NODE" to ("node_id" to maxNodeId),
            "NODEPATH" to ("nodepath_id" to maxNodepathId)
        )
        options
            .onEach {
                // 校验数据
                if (!key2Value.containsKey(it.type) || !key2Value.containsKey(it.target)) {
                    throw Error("JSON格式不正确！")
                }

                parse[it.target]?.let { (elem, maxId) ->
                    // 更改"相对"ID到绝对ID
                    if ((it.data[elem] as Int) < 0)
                        it.data[elem] = abs(it.data[elem] as Int) + maxId
                    // 当为路径的时候，其两个节点ID也可能是相对的
                    if (elem == "nodepath_id") {
                        if (it.data.containsKey("node1_id") && (it.data["node1_id"] as Int) < 0)
                            it.data["node1_id"] = abs(it.data["node1_id"] as Int) + maxNodeId
                        if (it.data.containsKey("node2_id") && (it.data["node2_id"] as Int) < 0)
                            it.data["node2_id"] = abs(it.data["node2_id"] as Int) + maxNodeId
                    }
                    else if (elem == "line_id") {
                        it.data["start_node_id"]?.let { that ->
                            val id = that as Int
                            if (id < 0)
                                it.data["start_node_id"] = abs(that) + maxNodeId
                        }
                        // 为线路的时候，还需要对节点和路径进行改变
                        it.data["bus_id_list"]?.let { that ->
                            val busIds = that as MutableList<Int>
                            for (i in 0 until busIds.size)
                                if (busIds[i] < 0)
                                    busIds[i] = abs(busIds[i]) + maxBusId
                        }
                        it.data["nodepath_id_list"]?.let { that ->
                            val nodepathIds = that as MutableList<Int>
                            for (i in 0 until nodepathIds.size)
                                if (nodepathIds[i] < 0)
                                    nodepathIds[i] = abs(nodepathIds[i]) + maxNodepathId
                        }
                    }
                }
            }
            .groupBy { it.type }
            .let { map ->
                insertData(map["INSERT"]?.run { sortedBy { key2Value[it.target]!! } })
                updateData(map["UPDATE"]?.run { sortedBy { key2Value[it.target]!! } })
                deleteData(map["DELETE"]?.run { sortedBy { -key2Value[it.target]!! } })
            }
    }

    private fun insertData(options: List<Option>?) =
        options?.forEach {
            val data = it.data
            when (it.target) {
                "PLACE" ->
                    placeMapper.insertPlace(
                        data["adcode"] as Int,
                        it.data["place_name"] as String,
                        BigDecimal(data["lat"] as Double),
                        BigDecimal(data["lont"] as Double))
                "BUS" ->
                    busMapper.addBus(
                        data["bus_id"] as Int,
                        data["bus_number"] as String,
                        data["bus_type"] as String
                    )
                "NODE" ->
                    nodeMapper.insertNode(
                        data["node_id"] as Int,
                        BigDecimal(data["lat"] as Double),
                        BigDecimal(data["lont"] as Double),
                        data["isStation"] as Boolean,
                        data["adcode"] as Int
                    )
                "NODEPATH" ->
                    nodepathMapper.addNodepath(
                        data["nodepath_id"] as Int,
                        data["node1_id"] as Int,
                        data["node2_id"] as Int,
                        data["speed_limit"] as Int,
                        data["adcode"] as Int,
                        data["street_name"] as String
                    )
                "BUSLINE" ->
                    buslineMapper.addBusline(
                        data["line_id"] as Int,
                        data["line_name"] as String,
                        data["start_node_id"] as Int,
                        (data["bus_id_list"] as List<Int>).joinToString (",","[","]"),
                        (data["nodepath_id_list"] as List<Int>).joinToString (",","[","]")
                    )
                "BUS_OF_PLACE" ->
                    placeMapper.addBusFromPlace(
                        data["bus_id"] as Int,
                        data["adcode"] as Int
                    )
                "BUSLINE_OF_PLACE" ->
                    placeMapper.addLineFromPlace(
                        data["line_id"] as Int,
                        data["adcode"] as Int
                    )
            }
        }

    private fun updateData(options: List<Option>?) =
        options?.forEach {
            val data = it.data
            when (it.target) {
                "PLACE" ->
                    placeMapper.updatePlace(
                        data["adcode"] as Int,
                        it.data["place_name"] as String,
                        BigDecimal(data["lat"] as Double),
                        BigDecimal(data["lont"] as Double)
                    )
                "BUS" ->
                    busMapper.updateBus(
                        data["bus_id"] as Int,
                        data["bus_number"] as String,
                        data["bus_type"] as String
                    )
                "NODE" ->
                    nodeMapper.updateNode(
                        data["node_id"] as Int,
                        BigDecimal(data["lat"] as Double),
                        BigDecimal(data["lont"] as Double),
                        data["isStation"] as Boolean,
                        data["adcode"] as Int
                    )
                "NODEPATH" ->
                    nodepathMapper.updateNodepath(
                        data["nodepath_id"] as Int,
                        data["node1_id"] as Int,
                        data["node2_id"] as Int,
                        data["speed_limit"] as Int,
                        data["adcode"] as Int,
                        data["street_name"] as String
                    )
                "BUSLINE" ->
                    buslineMapper.updateBusline(
                        data["line_id"] as Int,
                        data["line_name"] as String,
                        data["start_node_id"] as Int,
                        (data["bus_id_list"] as List<Int>).joinToString (",","[","]"),
                        (data["nodepath_id_list"] as List<Int>).joinToString (",","[","]")
                    )
            }
        }


    private fun deleteData(options: List<Option>?) =
        options?.forEach {
            val data = it.data
            when (it.target) {
                "PLACE" ->
                    placeMapper.deletePlace(
                        data["adcode"] as Int
                    )
                "BUS" ->
                    busMapper.deleteBus(
                        data["bus_id"] as Int
                    )
                "NODE" ->
                    nodeMapper.deleteNode(
                        data["node_id"] as Int
                    )
                "NODEPATH" ->
                    nodepathMapper.deleteNodepath(
                        data["nodepath_id"] as Int
                    )
                "BUSLINE" ->
                    buslineMapper.deleteBusline(
                        data["line_id"] as Int
                    )
                "BUS_OF_PLACE" ->
                    placeMapper.removeBusFromPlace(
                        data["bus_id"] as Int,
                        data["adcode"] as Int
                    )
                "BUSLINE_OF_PLACE" ->
                    placeMapper.removeLineFromPlace(
                        data["line_id"] as Int,
                        data["adcode"] as Int
                    )
            }
        }
}