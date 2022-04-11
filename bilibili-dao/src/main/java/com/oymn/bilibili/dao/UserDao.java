package com.oymn.bilibili.dao;

import com.oymn.bilibili.domain.User;
import com.oymn.bilibili.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;

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
}
