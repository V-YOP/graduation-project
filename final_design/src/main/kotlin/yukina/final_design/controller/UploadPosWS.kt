package yukina.final_design.controller

import org.slf4j.LoggerFactory
import javax.websocket.server.ServerEndpoint
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.stereotype.Component
import yukina.final_design.service.RealTimePosService
import java.sql.Timestamp
import java.util.concurrent.atomic.AtomicInteger
import javax.websocket.*

// 公交车向系统发送位置信息的websocket
// 使用原生websocket即可
@ServerEndpoint("/ws/uploadPos")
@Component
class UploadPosWS {
    var busId : Int? = null
    var busline : List<RealTimePosService.ParsedNodepath>? = null
    //连接建立时
    @OnOpen
    fun onOpen(session: Session) {
        onlineCount.incrementAndGet()
        log.info("当前总数是{}", onlineCount.get());
    }

    @OnClose
    fun onClose(session: Session?) {
        // 什么都不做
        onlineCount.decrementAndGet()
    }

    // 收到客户端的消息
    @OnMessage
    fun onMessage(message: String?, session: Session?) {
        /**
         * message的type是（使用Typescript的表示法）——
         * {
         *   bus_id : Int,
         *   line_id : Int, // 为什么需要line_id？因为公交车对应多个线路，应当限定当前公交车所在线路
         *   lat : Double,
         *   lont : Double,
         *   speed : Int
         * }[]
         */
        JSONObject(message).apply {
            val busId = getInt("bus_id")
            val lineId = getInt("line_id")
            val lat = getDouble("lat")
            val lont = getDouble("lont")
            val speed = getInt("speed")
            val timestamp = Timestamp(getLong("update_time"));
            if (this@UploadPosWS.busId == null) { // 第一次发送消息
                this@UploadPosWS.busId = busId
            }
            realTimePosService.updatePos(busId, lineId, lat, lont, speed, timestamp);
        }
        // log.info("收到消息：{}",message);
        // log.info("车辆ID：{}", busId)
    }

    @OnError
    fun onError(session: Session?, error: Throwable) {
        log.error("发生错误：{}", error)
        // busId?.let { realTimePosService.busOffline(it) }
        onlineCount.decrementAndGet()
    }
    @Component
    companion object {
        private val onlineCount = AtomicInteger(0)
        private val log = LoggerFactory.getLogger(UploadPosWS::class.java);
        // Websocket服务端是多例的
        // 因为必须要使用单例，所以应当放在伴生对象里，且必须使用注解在setter上。ugly
        private lateinit var realTimePosService: RealTimePosService
        @Autowired
        fun setRealTimePosService(realTimePosService: RealTimePosService)
            { this.realTimePosService = realTimePosService }
    }
}