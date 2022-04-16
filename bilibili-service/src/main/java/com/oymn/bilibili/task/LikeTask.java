package com.oymn.bilibili.task;

import com.oymn.bilibili.service.VideoLikeService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;


public class LikeTask extends QuartzJobBean {

    @Autowired
    private VideoLikeService videoLikeService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        
        //将Redis中的数据同步到数据库中
        videoLikeService.transLikedFromRedis2DB();
        videoLikeService.transLikedCountFromRedis2DB();
    }
}
