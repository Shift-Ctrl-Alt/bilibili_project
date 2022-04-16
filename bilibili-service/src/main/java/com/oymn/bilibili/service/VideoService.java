package com.oymn.bilibili.service;

import com.oymn.bilibili.constant.UserConstant;
import com.oymn.bilibili.constant.VideoConstant;
import com.oymn.bilibili.dao.UserCoinDao;
import com.oymn.bilibili.dao.VideoDao;
import com.oymn.bilibili.domain.*;
import com.oymn.bilibili.exception.ConditionException;
import com.oymn.bilibili.utils.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoService {

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @Autowired
    private UserCoinService userCoinService;
    
    @Autowired
    private UserService userService;

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

    /**
     * 添加视频收藏
     *
     * @param videoCollection
     */
    @Transactional
    public void addVideoCollection(VideoCollection videoCollection) {
        Long userId = videoCollection.getUserId();
        Long videoId = videoCollection.getVideoId();
        Long groupId = videoCollection.getGroupId();

        if (videoId == null || groupId == null) {
            throw new ConditionException("参数异常！");
        }

        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("非法视频！");
        }

        //删除原有视频收藏
        videoDao.deleteVideoCollection(userId, videoId);
        //添加新的视频收藏
        videoCollection.setCreateTime(new Date());
        videoDao.addVideoCollection(userId, videoId);
    }

    /**
     * 删除视频收藏
     *
     * @param userId
     * @param videoId
     */
    public void deleteVideoCollection(Long userId, Long videoId) {
        videoDao.deleteVideoCollection(userId, videoId);
    }

    /**
     * 获取视频收藏量
     *
     * @param userId
     * @param videoId
     * @return
     */
    public Map<String, Object> getVideoCollections(Long userId, Long videoId) {
        Long count = videoDao.getVideoCollections(userId, videoId);
        VideoCollection videoCollection = videoDao.getVideoCollectionByUserIdAndVideoId(userId, videoId);
        boolean like = videoCollection != null;

        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);

        return result;
    }

    /**
     * 视频投币
     *
     * @param videoCoin
     */
    public void addVideoCoins(VideoCoin videoCoin) {
        Long userId = videoCoin.getUserId();
        Long videoId = videoCoin.getVideoId();
        Integer amount = videoCoin.getAmount();
        if (videoId == null) {
            throw new ConditionException("参数异常！");
        }

        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("非法视频！");
        }

        //查询当前用户是否有足够的硬币
        Integer userCoinAmount = userCoinService.getUserCoinAmount(userId);
        userCoinAmount = userCoinAmount == null ? 0 : userCoinAmount;
        if (userCoinAmount < amount) {
            throw new ConditionException("硬币数量不足！");
        }

        //查询该用户是否已经对该视频投过币
        VideoCoin dbVideoCoin = videoDao.getVideoCoinByUserIdAndVideoId(userId, videoId);
        //之前没投过币了
        if (dbVideoCoin == null) {
            videoCoin.setCreateTime(new Date());
            videoDao.addVideoCoin(videoCoin);
        } else {
            //之前就已经投过币
            Integer dbAmount = dbVideoCoin.getAmount();
            dbAmount += amount;
            //如果投币数量大于系统规定的最大数
            if (dbAmount > VideoConstant.VIDEO_MAX_COIN) {
                throw new ConditionException("超过投币数量");
            } else {
                videoCoin.setUpdateTime(new Date());
                videoCoin.setAmount(dbAmount);
                videoDao.updateVideoCoin(videoCoin);
            }
        }

        //更新用户的硬币总数
        userCoinService.updateUserCoinAmount(userId, userCoinAmount - amount);
    }

    /**
     * 获取视频投币数量
     *
     * @param userId
     * @param videoId
     * @return
     */
    public Map<String, Object> getVideoCoins(Long userId, Long videoId) {
        if (videoId == null) {
            throw new ConditionException("参数异常！");
        }

        Integer count = videoDao.getVideoCoinsAmount(videoId);
        VideoCoin videoCoin = videoDao.getVideoCoinByUserIdAndVideoId(userId, videoId);
        boolean like = videoCoin != null;

        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);

        return result;
    }

    /**
     * 视频评论功能
     *
     * @param videoComment
     */
    public void addVideoComments(VideoComment videoComment) {

        Long videoId = videoComment.getVideoId();
        if (videoId == null) {
            throw new ConditionException("参数异常！");
        }

        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("非法视频！");
        }

        videoComment.setCreateTime(new Date());
        videoDao.addVideoComments(videoComment);
    }

    /**
     * 分页查询视频评论
     *
     * @param size
     * @param no
     * @param videoId
     * @return
     */
    public PageResult<VideoComment> pageListVideoComments(Integer size, Integer no, Long videoId) {
        if (videoId == null) {
            throw new ConditionException("参数异常！");
        }

        Video video = videoDao.getVideoById(videoId);
        if (video == null) {
            throw new ConditionException("非法视频");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("start", (no - 1) * size);
        params.put("limit", size);
        params.put("videoId", videoId);
        Integer total = videoDao.pageCountVideoComments(params);   //查询一级评论的数量  

        List<VideoComment> videoCommentList = new ArrayList<>();
        if (total > 0) {
            //查询一级评论
            videoCommentList = videoDao.pageListVideoComments(params);   
            
            //批量查询二级评论  
            List<Long> parentIdList = videoCommentList.stream().map(VideoComment::getId).collect(Collectors.toList());
            List<VideoComment> childCommentList = videoDao.batchGetVideoCommentByRootId(parentIdList);
            
            //批量查询用户信息
            Set<Long> userIdList = videoCommentList.stream().map(VideoComment::getId).collect(Collectors.toSet());
            Set<Long> replyIdList = childCommentList.stream().map(VideoComment::getId).collect(Collectors.toSet());
            userIdList.addAll(replyIdList);
            
            //批量获取用户信息
            List<UserInfo> userInfoList = userService.batchGetUserInfoByUserIds(userIdList);
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getUserId, userInfo -> userInfo));

            for (VideoComment parentComment : videoCommentList) {
                Long id = parentComment.getId();
                List<VideoComment> childList = new ArrayList<>();

                for (VideoComment childComment : childCommentList) {
                    if(id.equals(childComment.getRootId())){
                        childComment.setUserInfo(userInfoMap.get(childComment.getUserId()));
                        childComment.setReplyUserInfo(userInfoMap.get(childComment.getReplyUserId()));
                        childList.add(childComment);
                    }
                }
                
                parentComment.setChildList(childList);
                parentComment.setUserInfo(userInfoMap.get(parentComment.getUserId()));
            }
        }
        
        return new PageResult<>(total, videoCommentList);
    }

    /**
     * 查询视频详情
     * @param videoId
     * @return
     */
    public Map<String, Object> getVideoDetails(Long videoId) {
        
        Video video = videoDao.getVideoDetails(videoId);
        
        Long userId = video.getUserId();
        User user = userService.getUserInfo(userId);
        UserInfo userInfo = user.getUserInfo();
        
        Map<String, Object> result = new HashMap<>();
        result.put("video", video);
        result.put("userInfo", userInfo);

        return result;
    }
    
}
