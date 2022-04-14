package com.oymn.bilibili.dao;

import com.oymn.bilibili.domain.Video;
import com.oymn.bilibili.domain.VideoTag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoDao {
    void addVideos(Video video);

    void batchAddVideoTag(List<VideoTag> videoTagList);

    int pageCountVideos(Map<String, Object> params);

    List<Video> pageListVideos(Map<String, Object> params);
}
