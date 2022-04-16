package com.oymn.bilibili.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

@Mapper
public interface UserCoinDao {


    Integer getUserCoinAmount(Long userId);

    void updateUserCoinAmount(Long userId, Integer coinAmount, Date updateTime);
    
}
