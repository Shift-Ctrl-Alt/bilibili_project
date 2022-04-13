package com.oymn.bilibili.dao;

import com.oymn.bilibili.domain.auth.UserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserRoleDao {

    List<UserRole> getUserRoleByUserId(Long userId);

    void addUserRole(UserRole userRole);
}
