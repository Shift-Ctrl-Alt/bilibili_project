package com.oymn.bilibili.dao;

import com.oymn.bilibili.domain.VideoLike;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VideoLikeDao {
    void saveVideoLike(List<VideoLike> videoLikeList);

    void deleteVideoLike(VideoLike videoLike);


    VideoLike getVideoLikesByUserIdAndVideoId(Long userId, Long videoId);

    void insertVideoLike(VideoLike videoLike);
    
}
