package com.oymn.bilibili.api.aspect;

import com.oymn.bilibili.api.UserMomentsApi;
import com.oymn.bilibili.api.support.UserSupport;
import com.oymn.bilibili.constant.AuthRoleConstant;
import com.oymn.bilibili.constant.UserMomentsConstant;
import com.oymn.bilibili.domain.UserMoment;
import com.oymn.bilibili.domain.annotation.ApiLimitedRole;
import com.oymn.bilibili.domain.auth.UserRole;
import com.oymn.bilibili.exception.ConditionException;
import com.oymn.bilibili.service.UserMomentsService;
import com.oymn.bilibili.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//用于角色的数据权限控制
@Aspect
@Order(1)   //优先级
@Component
public class DataLimitedAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    @Pointcut("@annotation(com.oymn.bilibili.domain.annotation.DataLimited)")
    public void check() {
    }

    @Before("check()")
    public void doBefore(JoinPoint joinPoint) {
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            //如果是发动态
            if (arg instanceof UserMoment) {
                UserMoment userMoment = (UserMoment) arg;
                String type = userMoment.getType();
                //如果是Lv1等级并且发的不是视频，那么会被拦截
                if(roleCodeSet.contains(AuthRoleConstant.ROLE_LV1) && !UserMomentsConstant.MOMENTS_VIDEO.equals(type)){
                    throw new ConditionException("参数异常");
                }
                //这里可以继续补充各种等级的数据限制
            }
        }
    }
}
