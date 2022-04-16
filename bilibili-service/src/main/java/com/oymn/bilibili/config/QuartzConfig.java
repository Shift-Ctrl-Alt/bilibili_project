package com.oymn.bilibili.config;

import com.oymn.bilibili.task.LikeTask;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    private static final String LIKE_TASK_IDENTITY = "LikeTaskQuartz";

    @Bean
    public JobDetail quartzDetail(){
        return JobBuilder.newJob(LikeTask.class)
                .withIdentity(LIKE_TASK_IDENTITY)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger quartzTrigger(){
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInHours(1)  //每小时执行一次
                .repeatForever();
        
        return TriggerBuilder.newTrigger().forJob(quartzDetail())
                .withIdentity(LIKE_TASK_IDENTITY)
                .withSchedule(scheduleBuilder)
                .build();
    }

}
