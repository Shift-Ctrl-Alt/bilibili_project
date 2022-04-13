package com.oymn.bilibili.service;

import com.oymn.bilibili.dao.AuthRoleDao;
import com.oymn.bilibili.domain.auth.AuthRole;
import com.oymn.bilibili.domain.auth.AuthRoleElementOperation;
import com.oymn.bilibili.domain.auth.AuthRoleMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AuthRoleService {
    
    @Autowired
    private AuthRoleElementOperationService authRoleElementOperationService;
    
    @Autowired
    private AuthRoleMenuService authRoleMenuService;
    
    @Autowired
    private AuthRoleDao authRoleDao;
    
    //通过角色id获取有哪些能够可以操作的按钮
    public List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationService.getRoleElementOperationsByRoleIds(roleIdSet);
    }

    //通过角色id获取有哪些能够访问的页面
    public List<AuthRoleMenu> getRoleMenusByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuService.getRoleMenusByRoleIds(roleIdSet);
    }
    
    //通过角色唯一标识获取角色
    public AuthRole getRoleByCode(String code) {
        return authRoleDao.getRoleByCode(code);
    }
}
