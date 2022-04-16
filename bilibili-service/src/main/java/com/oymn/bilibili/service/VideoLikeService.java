package com.oymn.bilibili.service;

import com.oymn.bilibili.dao.VideoLikeDao;
import com.oymn.bilibili.domain.VideoData;
import com.oymn.bilibili.domain.VideoLike;
import com.oymn.bilibili.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoLikeService {

    @Autowired
    private VideoLikeDao videoLikeDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private VideoDataService videoDataService;

    //保存用户点赞记录
    private static final String MAP_KEY_USER_LIKED = "MAP_USER_LIKED";

    //保存每个视频被点赞数量
    private static final String MAP_KEY_USER_LIKED_COUNT = "MAP_USER_LIKED_COUNT";

    private static final Integer LIKED = 1;

    private static final Integer UNLIKED = 2;

    //点赞
    public void addVideoLike(VideoLike videoLike) {
        Long videoId = videoLike.getVideoId();
        Long userId = videoLike.getUserId();

        if (videoId == null || userId == null) {
            throw new ConditionException("参数异常！");
        }

        String key = generateLikeKey(userId, videoId);
        Integer value = (Integer) redisTemplate.opsForHash().get(MAP_KEY_USER_LIKED, key);

        //该用户对这个视频点过赞了
        if (value != null && value == LIKED) {
            throw new ConditionException("已经赞过了！");
        } else {
            //该用户对这个视频没点过赞
            //该用户对这个视频现如今是取消点赞的状态
            redisTemplate.opsForHash().put(MAP_KEY_USER_LIKED, key, LIKED);
            //让该视频的点赞数+1
            redisTemplate.opsForHash().increment(MAP_KEY_USER_LIKED_COUNT, videoId, 1);
        }
    }

    /**
     * 取消点赞
     *
     * @param videoLike
     */
    public void deleteVideoLike(VideoLike videoLike) {
        Long videoId = videoLike.getVideoId();
        Long userId = videoLike.getUserId();
        if (videoId == null || userId == null) {
            throw new ConditionException("参数异常！");
        }

        String key = this.generateLikeKey(userId, videoId);
        Integer value = (Integer) redisTemplate.opsForHash().get(MAP_KEY_USER_LIKED, key);

        if (value == null) {
            throw new ConditionException("当前不是点赞状态！");
        }

        redisTemplate.opsForHash().put(MAP_KEY_USER_LIKED, key, UNLIKED);
        redisTemplate.opsForHash().increment(MAP_KEY_USER_LIKED_COUNT, videoId, -1);
    }


    /**
     * 获取视频的点赞数
     *
     * @param userId
     * @param videoId
     * @return
     */
    public Map<String, Object> getVideoLikesCount(Long userId, Long videoId) {

        Integer rCount = (Integer) redisTemplate.opsForHash().get(MAP_KEY_USER_LIKED_COUNT, videoId);
        Long dbCount = videoDataService.getVideoLikesCount(videoId);
        VideoLike dbVideoLike = videoLikeDao.getVideoLikesByUserIdAndVideoId(userId, videoId);
        boolean like = dbVideoLike != null;

        Map<String, Object> result = new HashMap<>();
        result.put("count", rCount + dbCount);
        result.put("like", like);

        return result;
    }

    //将Redis中的点赞数据同步到数据库中
    @Transactional
    public void transLikedFromRedis2DB() {
        List<VideoLike> likeList = this.getLikedDataFromRedis();

        for (VideoLike videoLike : likeList) {
            Long userId = videoLike.getUserId();
            Long videoId = videoLike.getVideoId();
            Integer status = videoLike.getStatus();

            VideoLike dbVideoLike = videoLikeDao.getVideoLikesByUserIdAndVideoId(userId, videoId);
            //这是一条点赞记录
            if (status == LIKED) {
                //数据库中没有这条记录，插入
                if (dbVideoLike == null) {
                    videoLikeDao.insertVideoLike(videoLike);
                }
            } else {
                //这是一条取消点赞的记录
                //数据库中存在这条记录，删除
                if (dbVideoLike != null) {
                    videoLikeDao.deleteVideoLike(videoLike);
                }
            }
        }
    }

    //将Redis中的点赞数量同步到数据库中
    @Transactional
    public void transLikedCountFromRedis2DB() {
        List<VideoData> videoDataList = getLikedCountFromRedis();
        for (VideoData videoData : videoDataList) {
            Long videoId = videoData.getVideoId();
            Long likedCount = videoData.getLikedCount();

            VideoData dbVideoData = videoDataService.getVideoDataByVideoId(videoId);
            if (dbVideoData != null) {
                Long dbLikedCount = videoData.getLikedCount();
                likedCount += dbLikedCount;
                videoDataService.updateLikedCount(videoId, likedCount);
            } else {
                videoDataService.insertLikedCount(videoId, likedCount);
            }
        }
    }

    private List<VideoData> getLikedCountFromRedis() {

        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(MAP_KEY_USER_LIKED_COUNT, ScanOptions.NONE);
        List<VideoData> list = new ArrayList<>();
        while (cursor.hasNext()) {
            Map.Entry<Object, Object> map = cursor.next();
            Long videoId = (Long) map.getKey();
            Long likedCount = (Long) map.getValue();
            
            VideoData videoData = new VideoData();
            videoData.setVideoId(videoId);
            videoData.setLikedCount(likedCount);
            list.add(videoData);
            
            //从Redis中删除这条记录
            redisTemplate.opsForHash().delete(MAP_KEY_USER_LIKED_COUNT, videoData);
        }
        return list;
    }

    //获取Redis中的点赞数据
    List<VideoLike> getLikedDataFromRedis() {

        //获取到存放点赞数据的Hash
        Cursor<Map.Entry<String, Integer>> cursor = redisTemplate.opsForHash().scan(MAP_KEY_USER_LIKED, ScanOptions.NONE);

        List<VideoLike> videoLikeList = new ArrayList<>();
        while (cursor.hasNext()) {
            Map.Entry<String, Integer> entry = cursor.next();
            String LikeKey = entry.getKey();
            Integer status = entry.getValue();

            String[] str = this.splitLikeKey(LikeKey);
            Long userId = Long.valueOf(str[0]);
            Long videoId = Long.valueOf(str[1]);

            //组装成 VideoLike 对象
            VideoLike videoLike = new VideoLike(userId, videoId, status);
            videoLikeList.add(videoLike);
            //存到 list 后从 Redis 中删除
            redisTemplate.opsForHash().delete(MAP_KEY_USER_LIKED, LikeKey);
        }

        return videoLikeList;
    }

    private String generateLikeKey(Long userId, Long videoId) {
        return userId + "::" + videoId;
    }

    private String[] splitLikeKey(String key) {
        return key.split("::");
    }
}
