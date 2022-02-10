package yukina.final_design.controller

import com.alibaba.fastjson.JSON
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.configurationprocessor.json.JSONArray
import org.springframework.boot.configurationprocessor.json.JSONException
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.stereotype.Component
import yukina.final_design.service.RealTimePosService
import yukina.final_design_deprecated.controller.GetPosWB
import java.lang.Exception
import java.util.concurrent.atomic.AtomicInteger
import javax.websocket.*
import javax.websocket.server.PathParam
import javax.websocket.server.ServerEndpoint
import java.util.*
import kotlinx.coroutines.*
import yukina.final_design.dao.BusMapper
import yukina.final_design.dao.OverspeedPos
import yukina.final_design.dao.Pos
import yukina.final_design.service.InfoGetterService
import kotlin.system.exitProcess

// 前端调用的websocket
// type为"adcode"或"busList"，大小写不限 TODO
// 这里的busList是json格式的参数，标识所要监听的公交车的id的list
@ServerEndpoint("/ws/getPos")
@Component
class GetPosWS {
    @Component
    companion object {
        private val log = LoggerFactory.getLogger(GetPosWS::class.java);
        // Websocket服务端是多例的
        // 因为必须要使用单例，所以应当放在伴生对象里，且必须使用注解在setter上。ugly
        private lateinit var realTimePosService: RealTimePosService
        @Autowired
        fun setRealTimePosService(realTimePosService: RealTimePosService)
        { this.realTimePosService = realTimePosService }
        private lateinit var infoGetterService: InfoGetterService
        @Autowired
        fun setInfoGetterService(infoGetterService: InfoGetterService)
        { this.infoGetterService = infoGetterService }
    }
    lateinit var busList : List<Int>
    // 用来间隔时间发送信息的协程
    var job : Job? = null;
    fun init(busList: List<Int>, session: Session) =
        busList.also {
            this.busList = it;
            sendInfo(realTimePosService.collectResult(it), session)
            job?.cancel()
            job = GlobalScope.launch {
                while (isActive) {
                    delay(1000);
                    val res = realTimePosService.collectRealtimeResult(it)
                    println(res);
                    if (session.isOpen) // 需要在这里进行一次检查
                        sendInfo(res, session)
                }
            }
        }
    @OnOpen
    fun onOpen(session: Session) {
        log.info("建立连接！")
        // 摸了
    }

    @OnClose
    fun onClose(session: Session) {
        log.info("断开连接！")
        job?.cancel()
    }


    @OnMessage
    fun onMessage(message: String, session: Session) {
        println("收到消息！")
        /**
         *  收到消息，消息格式为
         *  {
         *      type : "adcode" | "busList",
         *      data : number | number[]
         *  }
         *  收到消息则要更新busList
         */
        JSON.parseObject(message)
            .let { jsonObject ->
                when (jsonObject["type"] as String) {
                    "adcode" -> {
                        jsonObject
                            .getInteger("data")
                            .let (infoGetterService::getBusListByAdcode)
                    }
                    "busList" -> {
                        jsonObject
                            .getJSONArray("data")
                            .toJavaList(Int::class.java)
                    }
                    else -> {
                        null
                    }
                }
            }?.let {
                init(it, session)
            }

    }



    @OnError
    fun onError(session: Session, error: Throwable) {
        log.error("发生错误")
        job?.cancel()
        error.printStackTrace()
    }

    /**
     * 服务端发送消息给客户端
     */
    private fun sendMessage(message: Any, toSession: Session) {
        try {
            toSession.basicRemote.sendText(JSON.toJSONString(message))
        } catch (e: Exception) {
            log.error("服务端发送消息给客户端失败：{}", e)
        }
    }
    private fun sendInfo(info :  Pair<List<Pos>?, List<OverspeedPos>?>, session: Session) =
        info.let {
            val first = it.first ?: listOf()
            val second = it.second ?: listOf()
            sendMessage(mapOf(
                "poses" to first,
                "overspeedPoses" to second
            ), session)
        }
}