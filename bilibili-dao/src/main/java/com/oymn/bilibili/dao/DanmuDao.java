package com.oymn.bilibili.dao;

import com.oymn.bilibili.domain.Danmu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface DanmuDao {


    void addDanmu(Danmu danmu);

    List<Danmu> getDanmus(Map<String, Object> params);
}
