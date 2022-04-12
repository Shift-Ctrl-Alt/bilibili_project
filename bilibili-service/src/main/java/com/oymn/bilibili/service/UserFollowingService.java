package com.oymn.bilibili.service;

import com.oymn.bilibili.constant.UserConstant;
import com.oymn.bilibili.dao.UserFollowingDao;
import com.oymn.bilibili.domain.FollowingGroup;
import com.oymn.bilibili.domain.User;
import com.oymn.bilibili.domain.UserFollowing;
import com.oymn.bilibili.domain.UserInfo;
import com.oymn.bilibili.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.ws.soap.Addressing;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserFollowingService {
    
    @Autowired
    private UserFollowingDao userFollowingDao;
    
    @Autowired
    private FollowingGroupService followingGroupService;
    
    @Autowired
    private UserService userService;

    //添加关注
    @Transactional
    public void addUserFollowings(UserFollowing userFollowing) {
        Long groupId = userFollowing.getGroupId();
        if(groupId == null){    
            //设置默认分组
            FollowingGroup followingGroup = followingGroupService.getByType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            userFollowing.setGroupId(followingGroup.getId());
        }else{
            FollowingGroup followingGroup = followingGroupService.getById(groupId);
            if(followingGroup == null){
                throw new ConditionException("该关注分组不存在！");
            }
        }
        Long followingId = userFollowing.getFollowingId();
        User user = userService.getUserById(followingId);
        if (user == null) {
            throw new ConditionException("关注的用户不存在！");
        }
        
        userFollowingDao.deleteUserFollowing(userFollowing.getUserId(), followingId);
        userFollowing.setCreateTime(new Date());
        userFollowingDao.addUserFollowing(userFollowing);
        
    }

    //列出所有关注
    public List<FollowingGroup> getUserFollowings(Long userId) {

        //获取关注的用户列表
        List<UserFollowing> userFollowingList = userFollowingDao.getUserFollowings(userId);
        Set<Long> followingIdSet = userFollowingList.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());
        
        List<UserInfo> userInfoList = new ArrayList<>();
        if(followingIdSet.size() > 0){
            //根据关注用户的id查询关注用户的基本信息
            userInfoList = userService.getUserInfoByUserIds(followingIdSet);
        }
        for(UserFollowing userFollowing : userFollowingList){
            for(UserInfo userInfo : userInfoList){
                if(userFollowing.getFollowingId().equals(userInfo.getUserId())){
                    userFollowing.setUserInfo(userInfo);
                }
            }
        }

        //将关注用户按关注分组进行分类
        List<FollowingGroup> followingGroupList = followingGroupService.getByUserId(userId);
        //创建一个全部关注的分组
        FollowingGroup allGroup = new FollowingGroup();
        allGroup.setName(UserConstant.USER_FOLLOWING_GROUP_ALL_NAME);
        allGroup.setFollowingUserInfoList(userInfoList);
        
        List<FollowingGroup> result = new ArrayList<>();
        result.add(allGroup);
        for (FollowingGroup group : followingGroupList) {
            List<UserInfo> list = new ArrayList<>();
            for (UserFollowing userFollowing : userFollowingList) {
                if(group.getId().equals(userFollowing.getGroupId())){
                    list.add(userFollowing.getUserInfo());
                }
            }
            group.setFollowingUserInfoList(list);
            result.add(group);
        }
        
        return result;
    }

    public List<UserFollowing> getUserFans(Long userId) {
        //查询粉丝列表
        List<UserFollowing> fanList = userFollowingDao.getUserFans(userId);
        Set<Long> fanIdSet = fanList.stream().map(UserFollowing::getUserId).collect(Collectors.toSet());
        //根据粉丝Id查询粉丝的详细信息
        List<UserInfo> userInfoList = new ArrayList<>();
        if(fanIdSet.size() > 0){
            userInfoList = userService.getUserInfoByUserIds(fanIdSet);
        }
        
        //查看当前用户是否关注了该粉丝
        List<UserFollowing> followingList = userFollowingDao.getUserFollowings(userId);
        for (UserFollowing fan : fanList) {
            //将粉丝关联其详细信息
            for(UserInfo userInfo : userInfoList){
                if(fan.getUserId().equals(userInfo.getUserId())){
                    userInfo.setFollowed(false);
                    fan.setUserInfo(userInfo);
                }
            }
            //如果同样有关注这个粉丝，将followed设置为true
            for(UserFollowing following : followingList){
                if(following.getFollowingId().equals(fan.getUserId())){
                    fan.getUserInfo().setFollowed(true);
                }
            }
        }
        return fanList;
    }

    public Long addUserFollowingGroups(FollowingGroup followingGroup) {
        
        followingGroup.setCreateTime(new Date());
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_USER);   //自定义分组类型
        followingGroupService.addFollowingGroups(followingGroup);
        return followingGroup.getId();
        
    }

    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupService.getFollowingGroups(userId);
    }
}
