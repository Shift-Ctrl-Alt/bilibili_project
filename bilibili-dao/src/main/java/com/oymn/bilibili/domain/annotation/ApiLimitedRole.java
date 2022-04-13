package com.oymn.bilibili.domain.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

//用于角色的接口权限控制
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Component
public @interface ApiLimitedRole {
    
    String[] limitedRoleCodeList() default {};
}
