package com.oymn.bilibili.dao.repository;

import com.oymn.bilibili.domain.Video;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VideoRepository extends ElasticsearchRepository<Video, Long> {

    /**
     * es会自动根据名称进行查询：find by title like keyword
     * @param keyword
     * @return
     */
    Video findByTitleLike(String keyword);
}
