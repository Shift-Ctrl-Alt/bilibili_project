package com.oymn.bilibili.service;

import com.oymn.bilibili.domain.VideoData;
import com.oymn.bilibili.dao.VideoDataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoDataService {

    @Autowired
    private VideoDataDao videoDataDao;

    public Long getVideoLikesCount(Long videoId) {
        return videoDataDao.getVideoLikesCount(videoId);
    }


    public VideoData getVideoDataByVideoId(Long videoId) {
        return videoDataDao.getVideoDataByVideoId(videoId);
    }

    public void updateLikedCount(Long videoId, Long likedCount) {
        videoDataDao.updateLikedCount(videoId, likedCount);
    }

    public void insertLikedCount(Long videoId, Long likedCount) {
        videoDataDao.insertLikedCount(videoId, likedCount);
    }
}
