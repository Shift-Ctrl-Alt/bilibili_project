package com.oymn.bilibili.service;

import com.oymn.bilibili.dao.FollowingGroupDao;
import com.oymn.bilibili.domain.FollowingGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowingGroupService {
    
    @Autowired
    private FollowingGroupDao followingGroupDao;


    public FollowingGroup getByType(String type) {
        return followingGroupDao.getByType(type);
    }

    public FollowingGroup getById(Long id) {
        return followingGroupDao.getById(id);
    }


    public List<FollowingGroup> getByUserId(Long userId) {
        return followingGroupDao.getByUserId(userId);
    }

    public void addFollowingGroups(FollowingGroup followingGroup) {
        followingGroupDao.addFollowingGroup(followingGroup);
    }

    public List<FollowingGroup> getFollowingGroups(Long userId) {
        return followingGroupDao.getFollowingGroups(userId);
    }
}
