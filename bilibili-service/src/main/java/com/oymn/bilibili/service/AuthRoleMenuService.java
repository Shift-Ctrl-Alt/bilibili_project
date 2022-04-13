package com.oymn.bilibili.service;

import com.oymn.bilibili.dao.AuthRoleMenuDao;
import com.oymn.bilibili.domain.auth.AuthRoleMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AuthRoleMenuService {
    
    @Autowired
    private AuthRoleMenuDao authRoleMenuDao;
    
    public List<AuthRoleMenu> getRoleMenusByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuDao.getRoleMenusByRoleIds(roleIdSet);
    }
}
