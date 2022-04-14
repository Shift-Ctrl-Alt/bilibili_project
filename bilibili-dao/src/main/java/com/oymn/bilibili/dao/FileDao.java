package com.oymn.bilibili.dao;

import com.oymn.bilibili.domain.File;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileDao {

    File getFileByMD5(String fileMD5);

    void addFile(File dbFile);
}
