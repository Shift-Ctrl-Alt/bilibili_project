package com.oymn.bilibili.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.util.StringUtils;
import com.oymn.bilibili.dao.DanmuDao;
import com.oymn.bilibili.domain.Danmu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DanmuService {
    
    @Autowired
    private DanmuDao danmuDao;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public void addDanmu(Danmu danmu){
        danmuDao.addDanmu(danmu);
    }

    @Async
    public void asyncAddDanmu(Danmu danmu){
        danmuDao.addDanmu(danmu);
    }
    
    public List<Danmu> getDanmus(Map<String, Object> params){
        return danmuDao.getDanmus(params);
    }
    
    public void addDanmuToRedis(Danmu danmu){
        String key = "danmu-video-" + danmu.getVideoId();
        String value = redisTemplate.opsForValue().get(key);
        
        //获取这个视频的全部弹幕
        List<Danmu> list = new ArrayList<>();
        if(!StringUtils.isNullOrEmpty(value)){
            list = JSONArray.parseArray(value, Danmu.class);
        }
        //添加弹幕
        list.add(danmu);
        
        //重新放回redis中
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(list));
    }
}
