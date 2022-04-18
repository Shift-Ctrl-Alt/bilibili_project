package com.oymn.bilibili.dao.repository;

import com.oymn.bilibili.domain.UserInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserInfoRepository extends ElasticsearchRepository<UserInfo, Long> {
    
    
}
