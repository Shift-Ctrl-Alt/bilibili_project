package com.oymn.bilibili.utils;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.CountDownLatch2;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.concurrent.TimeUnit;

public class RocketMQUtil {
    
    //同步发送消息
    public static void syncSendMsg(DefaultMQProducer producer, Message message) throws MQBrokerException, RemotingException, InterruptedException, MQClientException {
        SendResult result = producer.send(message);
        System.out.println(result);
    }
    
    //异步发送消息
    public static void asyncSendMsg(DefaultMQProducer producer, Message message) throws RemotingException, InterruptedException, MQClientException {
        int messageCount = 2;
        CountDownLatch2 countDownLatch = new CountDownLatch2(messageCount);
        
        for(int i = 0; i < messageCount; i++){
            producer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    countDownLatch.countDown();
                    System.out.println(sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable e) {
                    countDownLatch.countDown();
                    System.out.println("发送消息时发生了异常  " + e);
                    e.printStackTrace();
                }
            });
        }
        
        countDownLatch.await(5, TimeUnit.SECONDS);
    }
}
