package com.oymn.bilibili.api;

import com.oymn.bilibili.api.support.UserSupport;
import com.oymn.bilibili.constant.AuthRoleConstant;
import com.oymn.bilibili.domain.JsonResponse;
import com.oymn.bilibili.domain.UserMoment;
import com.oymn.bilibili.domain.annotation.ApiLimitedRole;
import com.oymn.bilibili.domain.annotation.DataLimited;
import com.oymn.bilibili.service.UserMomentsService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserMomentsApi {
    
    @Autowired
    private UserMomentsService userMomentsService;
    
    @Autowired
    private UserSupport userSupport;
    
    //发布动态
    @ApiLimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_LV0})    //限制Lv0不能发布动态
    @DataLimited  //限制Lv1发布动态只能是视频类型
    @PostMapping("user-moments")
    public JsonResponse<String> addUserMoments(@RequestBody UserMoment userMoment) throws MQBrokerException, RemotingException, InterruptedException, MQClientException {
        Long userId = userSupport.getCurrentUserId();
        userMoment.setUserId(userId);
        userMomentsService.addUserMoments(userMoment);
        return JsonResponse.success();
    }
    
    //获取订阅的动态
    @ApiLimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_LV0})    //限制Lv0不能获取动态
    @GetMapping("user-subscribed-moments")
    public JsonResponse<List<UserMoment>> getUserSubscribedMoments(){
        Long userId = userSupport.getCurrentUserId();
        List<UserMoment> userMomentList = userMomentsService.getUserSubscribedMoments(userId);
        return new JsonResponse<>(userMomentList);
    }
    
}
