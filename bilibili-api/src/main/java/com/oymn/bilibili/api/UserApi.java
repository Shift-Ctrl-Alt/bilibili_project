package com.oymn.bilibili.api;

import com.oymn.bilibili.api.support.UserSupport;
import com.oymn.bilibili.domain.JsonResponse;
import com.oymn.bilibili.domain.User;
import com.oymn.bilibili.domain.UserInfo;
import com.oymn.bilibili.service.UserService;
import com.oymn.bilibili.utils.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class UserApi {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserSupport userSupport;
    
    //获取当前登录用户的信息
    @GetMapping("/users")
    public JsonResponse<User> getUserInfo(){
        Long userId = userSupport.getCurrentUserId();
        User user = userService.getUserInfo(userId);
        return new JsonResponse<>(user);
    }

    //获取RSA公钥
    //这里的pk指public Key
    @GetMapping("/rsa-pks")
    public JsonResponse<String> getRsaPublicKey(){
        String pk = RSAUtil.getPublicKeyStr();
        return new JsonResponse<>(pk);
    }

    //注册
    @PostMapping("/users")
    public JsonResponse<String> addUser(@RequestBody User user){
        userService.addUser(user);
        return JsonResponse.success();
    }
    
    //登录
    @PostMapping("/user-tokens")
    public JsonResponse<String> login(@RequestBody User user) throws Exception {
        String token = userService.login(user);
        return new JsonResponse<>(token);
    }
    
    //更新User
    @PutMapping("/users")
    public JsonResponse<String> updateUser(@RequestBody User user) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        user.setId(userId);
        userService.updateUser(user);
        return JsonResponse.success();
    }
    
    //更新UserInfo
    @PutMapping("/user-info")
    public JsonResponse<String> updateUserInfo(@RequestBody UserInfo userInfo){
        Long userId = userSupport.getCurrentUserId();
        userInfo.setUserId(userId);
        userService.updateUserInfo(userInfo);
        return JsonResponse.success();
    }
    
    //双token实现登录功能（对上面登录接口的升级）
    @PostMapping("/user-dts")   //double tokens
    public JsonResponse<Map<String, Object>> loginForDts(@RequestBody User user) throws Exception {
        Map<String, Object> map = userService.loginForDts(user);
        return new JsonResponse<>(map);
    }
    
    //退出登录
    @DeleteMapping("/refresh-tokens")
    public JsonResponse<String> logout(HttpServletRequest request){
        String refreshToken = request.getHeader("refreshToken");
        Long userId = userSupport.getCurrentUserId();
        userService.logout(refreshToken, userId);
        return JsonResponse.success();
    }
    
    //重新刷新accessToken，延长登录时间
    @PostMapping("/access-tokens")
    public JsonResponse<String> refreshAccessToken(HttpServletRequest request) throws Exception {
        String refreshToken = request.getHeader("refreshToken");
        String accessToken = userService.refreshAccessToken(refreshToken);
        return new JsonResponse<>(accessToken);
    }
    
    
}
