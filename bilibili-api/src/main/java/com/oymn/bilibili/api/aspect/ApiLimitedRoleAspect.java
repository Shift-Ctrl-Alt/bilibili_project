package com.oymn.bilibili.api.aspect;

import com.oymn.bilibili.api.support.UserSupport;
import com.oymn.bilibili.domain.annotation.ApiLimitedRole;
import com.oymn.bilibili.domain.auth.UserRole;
import com.oymn.bilibili.exception.ConditionException;
import com.oymn.bilibili.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//用于角色的接口权限控制
@Aspect
@Order(1)   //优先级
@Component
public class ApiLimitedRoleAspect {
    
    @Autowired
    private UserSupport userSupport;
    
    @Autowired
    private UserRoleService userRoleService;
    
    @Pointcut("@annotation(com.oymn.bilibili.domain.annotation.ApiLimitedRole)")
    public void check(){
    }
    
    //注解上所标识的是被限制该接口的角色
    @Before("check() && @annotation(apiLimitedRole)")
    public void doBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole){
        Long userId = userSupport.getCurrentUserId();
        
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        String[] limitedRoleCodeList = apiLimitedRole.limitedRoleCodeList();

        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        Set<String> limitedRoleCodeSet = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
        roleCodeSet.retainAll(limitedRoleCodeSet);   //取交集
        
        if(roleCodeSet.size() > 0){
            throw new ConditionException("权限不足");
        }
    }
}
