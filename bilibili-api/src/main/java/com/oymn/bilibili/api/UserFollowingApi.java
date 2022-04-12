package com.oymn.bilibili.api;

import com.oymn.bilibili.api.support.UserSupport;
import com.oymn.bilibili.domain.FollowingGroup;
import com.oymn.bilibili.domain.JsonResponse;
import com.oymn.bilibili.domain.UserFollowing;
import com.oymn.bilibili.service.UserFollowingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserFollowingApi {
    
    @Autowired
    private UserFollowingService userFollowingService;
    
    @Autowired
    private UserSupport userSupport;

    
    //关注用户
    @PostMapping("/user-followings")
    public JsonResponse<String> addUserFollowings(@RequestBody UserFollowing userFollowing){
        Long userId = userSupport.getCurrentUserId();
        userFollowing.setUserId(userId);
        userFollowingService.addUserFollowings(userFollowing);
        return JsonResponse.success();
    }
    
    //获取用户的关注
    @GetMapping("/user-followings")
    public JsonResponse<List<FollowingGroup>> getUserFollowings(){
        Long userId = userSupport.getCurrentUserId();
        List<FollowingGroup> followingGroupList = userFollowingService.getUserFollowings(userId);
        return new JsonResponse<>(followingGroupList);
    }
    
    //获取用户的粉丝
    @GetMapping("/user-fans")
    public JsonResponse<List<UserFollowing>> getUserFans(){
        Long userId = userSupport.getCurrentUserId();
        List<UserFollowing> fans = userFollowingService.getUserFans(userId);
        return new JsonResponse<>(fans);
    }
    
    //添加新的分组
    @PostMapping("/user-following-groups")
    public JsonResponse<Long> addUserFollowingGroups(@RequestBody FollowingGroup followingGroup){
        Long userId = userSupport.getCurrentUserId();
        followingGroup.setUserId(userId);
        Long groupId = userFollowingService.addUserFollowingGroups(followingGroup);
        return new JsonResponse<>(groupId);
    }
    
    //显示用户的所有分组
    @GetMapping("/user-following-groups")
    public JsonResponse<List<FollowingGroup>> getUserFollowingGroups(){
        Long userId = userSupport.getCurrentUserId();
        List<FollowingGroup> followingGroupList = userFollowingService.getUserFollowingGroups(userId);
        return new JsonResponse<>(followingGroupList);
    }
    
    
}
