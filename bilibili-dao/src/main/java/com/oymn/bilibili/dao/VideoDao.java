package com.oymn.bilibili.dao;

import com.oymn.bilibili.domain.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoDao {
    void addVideos(Video video);

    void batchAddVideoTag(List<VideoTag> videoTagList);

    int pageCountVideos(Map<String, Object> params);

    List<Video> pageListVideos(Map<String, Object> params);

    Video getVideoById(Long videoId);

    void deleteVideoCollection(Long userId, Long videoId);

    void addVideoCollection(Long userId, Long videoId);

    Long getVideoCollections(Long userId, Long videoId);

    VideoCollection getVideoCollectionByUserIdAndVideoId(Long userId, Long videoId);

    VideoCoin getVideoCoinByUserIdAndVideoId(Long userId, Long videoId);

    void addVideoCoin(VideoCoin videoCoin);

    void updateVideoCoin(VideoCoin videoCoin);

    Integer getVideoCoinsAmount(Long videoId);

    void addVideoComments(VideoComment videoComment);

    List<VideoComment> pageListVideoComments(Map<String, Object> params);

    Integer pageCountVideoComments(Map<String, Object> params);

    List<VideoComment> batchGetVideoCommentByRootId(List<Long> parentIdList);

    Video getVideoDetails(Long videoId);
}
