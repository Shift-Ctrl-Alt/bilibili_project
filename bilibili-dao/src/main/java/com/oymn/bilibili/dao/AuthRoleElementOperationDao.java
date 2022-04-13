package com.oymn.bilibili.dao;

import com.oymn.bilibili.domain.auth.AuthRoleElementOperation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

@Mapper
public interface AuthRoleElementOperationDao {

    List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet);
}
