package com.oymn.bilibili.api;

import com.oymn.bilibili.api.support.UserSupport;
import com.oymn.bilibili.domain.JsonResponse;
import com.oymn.bilibili.domain.PageResult;
import com.oymn.bilibili.domain.Video;
import com.oymn.bilibili.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class VideoApi {
    
    @Autowired
    private VideoService videoService;
    
    @Autowired
    private UserSupport userSupport;

    /**
     * 视频投稿
     */
    @PostMapping("/videos")
    public JsonResponse<String> addVideos(@RequestBody Video video){
        Long userId = userSupport.getCurrentUserId();
        video.setUserId(userId);
        videoService.addVideos(video);
        return JsonResponse.success();
    }

    /**
     * 分页查询视频列表
     */
    @GetMapping("/videos")
    public JsonResponse<PageResult<Video>> pageListVideo(Integer size, Integer no, String area){
        PageResult<Video> result = videoService.pageListVideo(size, no, area);
        return new JsonResponse<>(result);
    }

    /**
     * 视频在线播放
     */
    @GetMapping("/video-slices")
    public void viewVideoOnlineBySlices(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String url) throws Exception {
        videoService.viewVideoOnlineBySlices(request, response, url);
    }
    
}   
