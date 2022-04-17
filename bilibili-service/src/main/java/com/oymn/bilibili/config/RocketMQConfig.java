package com.oymn.bilibili.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.util.StringUtils;
import com.oymn.bilibili.constant.RocketMQConstant;
import com.oymn.bilibili.constant.UserMomentsConstant;
import com.oymn.bilibili.domain.UserFollowing;
import com.oymn.bilibili.domain.UserMoment;
import com.oymn.bilibili.service.UserFollowingService;
import com.oymn.bilibili.service.websocket.WebSocketService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RocketMQConfig {
    
    @Value("${rocketmq.name.server.address}")
    private String nameServerAddress;
    
    @Autowired
    private UserFollowingService userFollowingService;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    //动态相关的生产者
    @Bean("momentsProducer")
    public DefaultMQProducer momentsProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(RocketMQConstant.GROUP_MOMENTS);
        producer.setNamesrvAddr(nameServerAddress);
        producer.start();
        return producer;
    }
    
    //动态相关的消费者
    @Bean("momentsConsumer")
    public DefaultMQPushConsumer momentsConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMQConstant.GROUP_MOMENTS);
        consumer.setNamesrvAddr(nameServerAddress);
        consumer.subscribe(RocketMQConstant.TOPIC_MOMENTS, "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt msg = msgs.get(0);
                if (msg == null) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                String bodyStr = new String(msg.getBody());
                UserMoment userMoment = JSONObject.toJavaObject(JSONObject.parseObject(bodyStr), UserMoment.class);
                Long userId = userMoment.getUserId();
                List<UserFollowing> fanList = userFollowingService.getUserFans(userId);
                for (UserFollowing fan : fanList) {
                    String key = "subscribed-" + fan.getUserId();
                    String subscribedListStr = redisTemplate.opsForValue().get(key);
                    List<UserMoment> subscribedList;
                    if(StringUtils.isNullOrEmpty(subscribedListStr)){
                        subscribedList = new ArrayList<>();
                    }else{
                        subscribedList = JSONArray.parseArray(subscribedListStr, UserMoment.class);
                    }
                    
                    subscribedList.add(userMoment);
                    redisTemplate.opsForValue().set(key, JSONObject.toJSONString(subscribedList));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }
    
    //弹幕相关的生产者
    @Bean("danmusProducer")
    public DefaultMQProducer danmusQProducer() throws MQClientException {
        //实例化消息生产者Producer
        DefaultMQProducer producer = new DefaultMQProducer(RocketMQConstant.GROUP_DANMUS);
        //设置NameServer的地址
        producer.setNamesrvAddr(nameServerAddress);
        //启动producer实例
        producer.start();
        return producer;
    }
    
    //弹幕相关的消费者
    @Bean("danmusConsumer")
    public DefaultMQPushConsumer danmusConsumer() throws MQClientException {
        //实例化消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMQConstant.GROUP_DANMUS);
        //设置NameServer的地址
        consumer.setNamesrvAddr(nameServerAddress);
        //订阅一个或多个topic，以及tag来过滤需要消费的消息
        consumer.subscribe(RocketMQConstant.TOPIC_DANMUS, "*");
        //注册回调实现类来处理从broker拉取回来的消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt msg = msgs.get(0);
                if(msg == null){
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                
                //获取消息体
                byte[] msgByte = msg.getBody();
                String bodyStr = new String(msgByte);
                JSONObject jsonObject = JSONObject.parseObject(bodyStr);
                
                //获取弹幕相关信息
                String sessionId = jsonObject.getString("sessionId");
                String message = jsonObject.getString("message");
                
                //将弹幕发送到相应的客户端
                WebSocketService webSocketService = WebSocketService.WEBSOCKET_MAP.get(sessionId);
                //判断要发送的客户端是否连接状态
                if(webSocketService.getSession().isOpen()){
                    try {
                        webSocketService.sendMessage(message);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                
                //表示该消息已经被成功消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        
        return consumer;
    }
}
