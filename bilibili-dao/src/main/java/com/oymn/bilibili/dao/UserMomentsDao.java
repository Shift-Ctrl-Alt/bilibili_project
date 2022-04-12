package com.oymn.bilibili.dao;

import com.oymn.bilibili.domain.UserMoment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMomentsDao {

    void addUserMoments(UserMoment userMoment);
}
