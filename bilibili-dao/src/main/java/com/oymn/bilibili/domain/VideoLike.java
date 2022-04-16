package com.oymn.bilibili.domain;

import java.util.Date;

public class VideoLike {

    private Long id;

    private Long userId;

    private Long videoId;

    private Date createTime;
    
    private Integer status;   //1表示点赞，2表示取消点赞

    public VideoLike(Long userId, Long videoId) {
        this.userId = userId;
        this.videoId = videoId;
    }
    public VideoLike(Long userId, Long videoId, Integer status) {
        this.userId = userId;
        this.videoId = videoId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
