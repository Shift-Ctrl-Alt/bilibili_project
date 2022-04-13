package com.oymn.bilibili.api;

import com.oymn.bilibili.api.support.UserSupport;
import com.oymn.bilibili.domain.JsonResponse;
import com.oymn.bilibili.domain.auth.UserAuthorities;
import com.oymn.bilibili.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAuthApi {
    
    @Autowired
    private UserSupport userSupport;
    
    @Autowired
    private UserAuthService userAuthService;
    
    //获取用户权限
    @GetMapping("user-authorities")
    public JsonResponse<UserAuthorities> getUserAuthorities(){
        Long userId = userSupport.getCurrentUserId();
        UserAuthorities userAuthorities = userAuthService.getUserAuthorities(userId);
        return new JsonResponse<>(userAuthorities);
    }
}
