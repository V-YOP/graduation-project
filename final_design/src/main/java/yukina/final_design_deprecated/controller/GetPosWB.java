package yukina.final_design_deprecated.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import yukina.final_design_deprecated.entity.Pos;
import yukina.final_design_deprecated.service.BusPosService;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

// 前端所使用的WB
@Slf4j
@ServerEndpoint("/ws/getPos")
@Component
public class GetPosWB {
    private static BusPosService busPosService;
    @Autowired
    public void setBusPosService(BusPosService busPosService) {
        GetPosWB.busPosService = busPosService;
    }

    private Integer[] busIdList;

    public Integer[] getBusIdList() {
        return busIdList;
    }

    public void setBusIdList(Integer[] busIdList) {
        this.busIdList = busIdList;
    }

    //连接建立时
    @OnOpen
    public void onOpen(Session session) {
        log.info("建立连接！");
    }
    @OnClose
    public void onClose(Session session) {
        log.info("断开连接！");
    }

    private String generateResp( String type) {
        List<Pos> poses;
        if (type.equals("present"))
            poses = busPosService.getPosOfIDsPresent(busIdList);
        else
            poses = busPosService.getPosOfIDsLong(busIdList);
        if (poses.size()==0) return "[]";
        return JSONObject.wrap(
                poses.stream().map(elem->
                        new HashMap<String, Object>(){{
                            put("bus_id", elem.getBus_id());
                            put("lat", elem.getLat());
                            put("lont", elem.getLont());
                            put("speed", elem.getSpeed());
                            put("update_time", elem.getUpdate_time());
                        }})
                        .collect(Collectors.toList()))
                .toString();
    }
    // 收到客户端的消息
    @OnMessage
    public void onMessage(String message, Session session) throws JSONException, InterruptedException {
        log.info("收到消息：{}",message);
        if (this.getBusIdList() == null) {
            JSONArray jsonObj = new JSONArray(message);
            Integer[] tmp = new Integer[jsonObj.length()];
            for (int i = 0; i < jsonObj.length(); i++) {
                tmp[i] = jsonObj.getInt(i);
            }
            this.setBusIdList(tmp);
            // 发送过去一段时间的这些公交车的位置信息
            sendMessage(generateResp(""), session);
        }
        // 收到消息，该消息将提供前端所需实时展示的公交车的id，并启动心跳
        // 这个待修改，当前的实现鲁棒性太差
        new Thread(()->{
            while (true) {
                if (!session.isOpen()) return;
                String msg = generateResp("present");
                sendMessage(msg, session);
                log.info("发送消息:{}", msg);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

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
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败：{}", e);
        }
    }
}
