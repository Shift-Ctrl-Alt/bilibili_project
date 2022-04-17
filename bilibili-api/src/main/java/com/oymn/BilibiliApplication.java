package com.oymn;

import com.oymn.bilibili.service.websocket.WebSocketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement  //开启事务相关功能
@EnableAsync        //开启异步相关功能
@EnableScheduling   //开启定时任务相关功能
public class BilibiliApplication {

    public static void main(String[] args){
        ApplicationContext app = SpringApplication.run(BilibiliApplication.class, args);
        
        //WebSocketService需要通过ApplicationContext来获取Bean
        WebSocketService.setApplicationContext(app);
    }
}
