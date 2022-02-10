package yukina.final_design_deprecated.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import yukina.final_design_deprecated.service.BusPosService;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.atomic.AtomicInteger;

// 公交车向系统发送位置信息的websocket
//TODO
@Slf4j
@ServerEndpoint("/ws/uploadPos")
@Component
public class UploadPosWB {
    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    private static BusPosService busPosService;
    @Autowired
    public void setBusPosService(BusPosService busPosService) {
        UploadPosWB.busPosService = busPosService;
    }
    //连接建立时
    @OnOpen
    public void onOpen(Session session) {
        onlineCount.incrementAndGet();
        //log.info("当前总数是{}", onlineCount.get());
    }
    @OnClose
    public void onClose(Session session) {
        onlineCount.decrementAndGet();
    }

    // 收到客户端的消息
    @OnMessage
    public void onMessage(String message, Session session) throws JSONException {

        JSONObject jsonObj = new JSONObject(message);
        if (!busPosService.illegal(jsonObj))
            busPosService.uploadMessage(jsonObj);
        //log.info("收到消息：{}",message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     * 服务端发送消息给客户端
     */
    private void sendMessage(String message, Session toSession) {
        try {
            log.info("服务端给客户端[{}]发送消息{}", toSession.getId(), message);
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            //log.error("服务端发送消息给客户端失败：{}", e);
        }
    }
}
