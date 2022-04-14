package com.oymn.bilibili.service;

import com.oymn.bilibili.dao.VideoDao;
import com.oymn.bilibili.domain.PageResult;
import com.oymn.bilibili.domain.Video;
import com.oymn.bilibili.domain.VideoTag;
import com.oymn.bilibili.exception.ConditionException;
import com.oymn.bilibili.utils.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
public class VideoService {

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    /**
     * 上传视频
     *
     * @param video
     */
    @Transactional
    public void addVideos(Video video) {

        Date now = new Date();
        video.setCreateTime(now);
        videoDao.addVideos(video);

        Long videoId = video.getId();
        List<VideoTag> tagList = video.getVideoTagList();
        for (VideoTag videoTag : tagList) {
            videoTag.setCreateTime(now);
            videoTag.setVideoId(videoId);
        }
        videoDao.batchAddVideoTag(tagList);
    }

    /**
     * 分页查询视频列表
     *
     * @param size
     * @param no
     * @param area
     * @return
     */
    public PageResult<Video> pageListVideo(Integer size, Integer no, String area) {
        if (size == null || no == null) {
            throw new ConditionException("参数异常！");
        }

        //封装参数
        Map<String, Object> params = new HashMap<>();
        params.put("start", (no - 1) * size);
        params.put("size", size);
        params.put("area", area);

        int total = videoDao.pageCountVideos(params);
        List<Video> videoList = new ArrayList<>();
        if (total > 0) {
            videoList = videoDao.pageListVideos(params);
        }

        return new PageResult<>(total, videoList);
    }

    /**
     * 在线播放视频
     *
     * @param request
     * @param response
     * @param url
     */
    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) throws Exception {
        fastDFSUtil.viewVideoOnlineBySlices(request, response, url);
    }
}
