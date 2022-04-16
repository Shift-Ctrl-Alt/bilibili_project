package com.oymn.bilibili.dao;

import com.oymn.bilibili.domain.RefreshTokenDetail;
import com.oymn.bilibili.domain.User;
import com.oymn.bilibili.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Mapper
public interface UserDao {
    
    void addUser(User user);

    void addUserInfo(UserInfo userInfo);

    User getUserByPhone(String phone);

    User getUserById(Long id);

    UserInfo getUserInfoByUserId(Long userId);

    Integer updateUser(User user);

    Integer updateUserInfo(UserInfo userInfo);

    User getUserByPhoneOrEmail(String phone, String email);

    List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList);

    void deleteRefreshToken(String refreshToken, Long userId);

    void addRefreshToken(String refreshToken, Long userId, Date creatTime);

    RefreshTokenDetail getRefreshTokenDetail(String refreshToken);

    List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList);
}
