package com.oymn.bilibili.service;

import com.oymn.bilibili.dao.UserCoinDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.DatabaseMetaData;
import java.util.Date;

@Service
public class UserCoinService {
    
    @Autowired
    private UserCoinDao userCoinDao;

    /**
     * 获取用户的硬币数量
     * @param userId
     * @return
     */
    public Integer getUserCoinAmount(Long userId) {
        return userCoinDao.getUserCoinAmount(userId);
    }

    /**
     * 更新用户的硬币数量
     * @param userId
     * @param coinAmount
     */
    public void updateUserCoinAmount(Long userId, Integer coinAmount) {
        userCoinDao.updateUserCoinAmount(userId, coinAmount, new Date());
    }
}
