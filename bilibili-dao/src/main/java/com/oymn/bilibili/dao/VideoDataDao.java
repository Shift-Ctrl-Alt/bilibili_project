package com.oymn.bilibili.dao;

import com.oymn.bilibili.domain.VideoData;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VideoDataDao {

    Long getVideoLikesCount(Long videoId);

    VideoData getVideoDataByVideoId(Long videoId);

    void updateLikedCount(Long videoId, Long likedCount);

    void insertLikedCount(Long videoId, Long likedCount);
}
