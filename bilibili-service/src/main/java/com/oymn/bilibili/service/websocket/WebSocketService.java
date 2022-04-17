package com.oymn.bilibili.service.websocket;

import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.util.StringUtils;
import com.oymn.bilibili.constant.RocketMQConstant;
import com.oymn.bilibili.domain.Danmu;
import com.oymn.bilibili.domain.JsonResponse;
import com.oymn.bilibili.service.DanmuService;
import com.oymn.bilibili.utils.RocketMQUtil;
import com.oymn.bilibili.utils.TokenUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/socket/{token}")
public class WebSocketService {

    //日志
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //在线人数
    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    //所有客户端
    public static final ConcurrentHashMap<String, WebSocketService> WEBSOCKET_MAP = new ConcurrentHashMap<>();

    //每个客户端含有一个session，用来与服务端通信
    private Session session;

    //唯一标识
    private String sessionId;

    private Long userId;

    //用来获取Bean
    private static ApplicationContext APPLICATION_CONTEXT;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        WebSocketService.APPLICATION_CONTEXT = applicationContext;
    }

    //连接成功后执行的方法
    @OnOpen
    public void openConnection(Session session, @PathParam("token") String token) {
        this.sessionId = session.getId();
        this.session = session;
        try {
            this.userId = TokenUtil.verifyToken(token);
        } catch (Exception e) {
        }

        //如果 map中已经含有该session
        if (WEBSOCKET_MAP.containsKey(sessionId)) {
            WEBSOCKET_MAP.remove(sessionId);
            WEBSOCKET_MAP.put(sessionId, this);
        } else {
            WEBSOCKET_MAP.put(sessionId, this);
            ONLINE_COUNT.getAndIncrement();   //在线人数加一
        }

        logger.info("用户连接成功：" + sessionId + "，当前在线人数：" + ONLINE_COUNT.get());

        try {
            //连接成功，回应前端
            this.sendMessage("0");
        } catch (Exception e) {
            logger.error("连接异常！");
        }
    }

    /**
     * 关闭连接时调用
     */
    @OnClose
    public void closeConnection() {
        if (WEBSOCKET_MAP.containsKey(this.sessionId)) {
            WEBSOCKET_MAP.remove(this.sessionId);
            ONLINE_COUNT.getAndDecrement();
        }

        logger.info("用户退出：" + this.sessionId + ",当前在线人数：" + ONLINE_COUNT.get());
    }

    /**
     * 接受前端数据时调用的方法
     *
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        logger.info("用户信息：" + sessionId + "，报文：" + message);

        if (!StringUtils.isNullOrEmpty(message)) {
            try {
                //将弹幕转发给其他客户端
                for (Map.Entry<String, WebSocketService> entry : WEBSOCKET_MAP.entrySet()) {
                    WebSocketService webSocketService = entry.getValue();
                    
                    //使用消息队列来削峰
                    DefaultMQProducer danmusProducer = (DefaultMQProducer) APPLICATION_CONTEXT.getBean("danmusProducer");
                    
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sessionId", this.sessionId);
                    jsonObject.put("message", message);
                    
                    //构造rocketmq所需要的消息结构
                    Message msg = new Message(RocketMQConstant.TOPIC_DANMUS, JSONObject.toJSONString(jsonObject).getBytes(StandardCharsets.UTF_8));
                    //异步发送到消息队列
                    RocketMQUtil.asyncSendMsg(danmusProducer, msg);
                }

                //只有登录了才可以发送弹幕
                if(this.userId != null){
                    Danmu danmu = JSONObject.parseObject(message, Danmu.class);
                    danmu.setUserId(this.userId);
                    danmu.setCreateTime(new Date());

                    DanmuService danmuService = (DanmuService) APPLICATION_CONTEXT.getBean("danmuService");
                    
                    //danmuService.addDanmu(danmu);
                    //优化：异步保存到数据库中
                    //还可以进一步优化：使用消息队列削峰（代实现）
                    danmuService.asyncAddDanmu(danmu);
                    
                    //保存到Redis中（为了查询更快）
                    danmuService.addDanmuToRedis(danmu);
                }
                
            } catch (Exception e) {
                logger.error("弹幕接受出现问题");
                e.printStackTrace();
            }
        }
    }

    /**
     * 当发生错误时调用的方法
     */
    @OnError
    public void onError(Throwable error) {
        
    }

    /**
     * 定时向客户端发送在线人数，时间间隔为5秒
     * 这里其实并没有分不同的视频（模拟地实现一下而已）
     * @throws IOException
     */
    @Scheduled(fixedRate = 5000)
    private void noticeOnlineCount() throws IOException {
        
        for(Map.Entry<String, WebSocketService> entry : WebSocketService.WEBSOCKET_MAP.entrySet()){
            //获取每一个客户端的websocketService
            WebSocketService webSocketService = entry.getValue();
            //判断是否还在线
            if(webSocketService.session.isOpen()){
                
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("onlineCount", ONLINE_COUNT.get());
                jsonObject.put("msg", "当前在线人数为" + ONLINE_COUNT.get());
                
                //向其发送在线人数
                webSocketService.sendMessage(jsonObject.toJSONString());
            }
        }
    }
    
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    public Session getSession() {
        return session;
    }

    public String getSessionId() {
        return sessionId;
    }
}
