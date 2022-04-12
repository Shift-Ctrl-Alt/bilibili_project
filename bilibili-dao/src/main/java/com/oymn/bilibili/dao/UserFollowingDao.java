package com.oymn.bilibili.dao;

import com.oymn.bilibili.domain.UserFollowing;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserFollowingDao {

    void deleteUserFollowing(Long userId, Long followingId);

    void addUserFollowing(UserFollowing userFollowing);

    List<UserFollowing> getUserFollowings(Long userId);

    List<UserFollowing> getUserFans(Long userId);
}
