package com.oymn.bilibili.service;

import com.mysql.cj.util.StringUtils;
import com.oymn.bilibili.dao.UserDao;
import com.oymn.bilibili.domain.RefreshTokenDetail;
import com.oymn.bilibili.domain.User;
import com.oymn.bilibili.constant.UserConstant;
import com.oymn.bilibili.domain.UserInfo;
import com.oymn.bilibili.exception.ConditionException;
import com.oymn.bilibili.utils.MD5Util;
import com.oymn.bilibili.utils.RSAUtil;
import com.oymn.bilibili.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;
    
    @Autowired
    private UserAuthService userAuthService;

    @Transactional
    public void addUser(User user) {
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new ConditionException("手机号码不能为空");
        }

        User dbUser = this.getUserByPhone(phone);
        if (dbUser != null) {
            throw new ConditionException("该手机号码已经被注册了");
        }

        Date now = new Date();
        String salt = String.valueOf(now.getTime());
        String password = user.getPassword();
        String rawPassword;

        try {
            //解密
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("密码解密失败");
        }

        //用MD5加密
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        user.setSalt(salt);
        user.setPassword(md5Password);
        user.setCreateTime(now);
        userDao.addUser(user);

        //添加用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setGender(UserConstant.GENDER_MALE);
        userInfo.setCreateTime(now);
        userDao.addUserInfo(userInfo);
        
        //给用户添加默认角色
        userAuthService.addUserDefaultRole(user.getId());
    }

    private User getUserByPhone(String phone) {
        return userDao.getUserByPhone(phone);
    }

    public String login(User user) throws Exception{
        String phone = user.getPhone() == null ? "" : user.getPhone();
        String email = user.getEmail() == null ? "" : user.getEmail();
        
        if(StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)){
            throw new ConditionException("参数异常！");
        }
        User dbUser = userDao.getUserByPhoneOrEmail(phone, email);
        if(dbUser == null){
            throw new ConditionException("当前用户不存在！");
        }
        
        String password = user.getPassword();
        String rawPassword;
        try{
            rawPassword = RSAUtil.decrypt(password);
        }catch (Exception e){
            throw new ConditionException("密码解密失败！");
        }
        
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        if(!md5Password.equals(dbUser.getPassword())){
            throw new ConditionException("密码错误！");
        }
        return TokenUtil.generateToken(dbUser.getId());
    }

    public User getUserInfo(Long userId) {
        
        User user = userDao.getUserById(userId);
        UserInfo userInfo = userDao.getUserInfoByUserId(userId);
        user.setUserInfo(userInfo);
        return user;
    }

    public void updateUser(User user) throws Exception {
        Long id = user.getId();
        User dbUser = userDao.getUserById(id);
        if (dbUser == null) {
            throw new ConditionException("用户不存在！");
        }
        
        if(!StringUtils.isNullOrEmpty(user.getPassword())){
            String rawPassword = RSAUtil.decrypt(user.getPassword());
            String md5Password = MD5Util.sign(rawPassword, user.getSalt(), "UTF-8");
            user.setPassword(md5Password);
        }
        
        user.setUpdateTime(new Date());
        userDao.updateUser(user);
    }

    public void updateUserInfo(UserInfo userInfo) {
        userInfo.setUpdateTime(new Date());
        userDao.updateUserInfo(userInfo);
        
    }

    public User getUserById(Long followingId) {
        return userDao.getUserById(followingId);
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList) {
        return userDao.getUserInfoByUserIds(userIdList);
    }

    //双token的登录方式
    public Map<String, Object> loginForDts(User user) throws Exception {
        String phone = user.getPhone() == null ? "" : user.getPhone();
        String email = user.getEmail() == null ? "" : user.getEmail();

        if(StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)){
            throw new ConditionException("参数异常！");
        }
        User dbUser = userDao.getUserByPhoneOrEmail(phone, email);
        if(dbUser == null){
            throw new ConditionException("当前用户不存在！");
        }

        String password = user.getPassword();
        String rawPassword;
        try{
            rawPassword = RSAUtil.decrypt(password);
        }catch (Exception e){
            throw new ConditionException("密码解密失败！");
        }

        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        if(!md5Password.equals(dbUser.getPassword())){
            throw new ConditionException("密码错误！");
        }
        
        Long userId = dbUser.getId();
        //用于验证的token
        String accessToken = TokenUtil.generateToken(userId);
        //用于刷新的token(作用：1.自动刷新token   2.退出登录)
        String refreshToken = TokenUtil.generateRefreshToken(userId);
        //保存refreshToken到数据库
        userDao.deleteRefreshToken(refreshToken, userId);
        userDao.addRefreshToken(refreshToken, userId, new Date());
        
        Map<String, Object> map = new HashMap<>();
        map.put("accessToken", accessToken);
        map.put("refreshToken", refreshToken);
        
        return map;
    }

    //退出登录
    public void logout(String refreshToken, Long userId) {
        userDao.deleteRefreshToken(refreshToken, userId);
    }

    //重新刷新accessToken
    public String refreshAccessToken(String refreshToken) throws Exception {
        RefreshTokenDetail refreshTokenDetail = userDao.getRefreshTokenDetail(refreshToken);
        if (refreshTokenDetail == null) {
            throw new ConditionException("555","refreshToken过期！");
        }
        Long userId = refreshTokenDetail.getUserId();
        return TokenUtil.generateToken(userId);
    }
}
