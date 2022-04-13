package com.oymn.bilibili.service;

import com.oymn.bilibili.constant.AuthRoleConstant;
import com.oymn.bilibili.domain.auth.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAuthService {
    
    @Autowired
    //用户的角色的相关服务
    private UserRoleService userRoleService;
    
    @Autowired
    //角色的权限的相关服务
    private AuthRoleService authRoleService;

    //获取用户权限
    public UserAuthorities getUserAuthorities(Long userId) {
        
        //查询用户的所有角色
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        Set<Long> roleIdSet = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
        
        //通过上面查到的角色查询所拥有的权限（访问页面权限和操作按钮权限）
        List<AuthRoleElementOperation> roleElementOperationList = authRoleService.getRoleElementOperationsByRoleIds(roleIdSet);
        List<AuthRoleMenu> roleMenuList = authRoleService.getRoleMenusByRoleIds(roleIdSet);
        
        //进行封装并返回
        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setRoleElementOperationList(roleElementOperationList);
        userAuthorities.setRoleMenuList(roleMenuList);
        
        return userAuthorities;
    }

    //给用户添加默认角色
    public void addUserDefaultRole(Long userId) {
        UserRole userRole = new UserRole();
        //添加Lv0等级的角色
        AuthRole role = authRoleService.getRoleByCode(AuthRoleConstant.ROLE_LV0);
        userRole.setRoleId(role.getId());
        userRole.setUserId(userId);
        userRoleService.addUserRole(userRole);
    }
}
