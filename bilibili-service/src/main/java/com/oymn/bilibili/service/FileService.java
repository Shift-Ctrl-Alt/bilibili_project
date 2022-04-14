package com.oymn.bilibili.service;

import com.mysql.cj.util.StringUtils;
import com.oymn.bilibili.dao.FileDao;
import com.oymn.bilibili.domain.File;
import com.oymn.bilibili.utils.FastDFSUtil;
import com.oymn.bilibili.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

@Service
public class FileService {
    
    @Autowired
    private FileDao fileDao;
    
    @Autowired
    private FastDFSUtil fastDFSUtil;
    
    public String getFileMD5(MultipartFile file) throws Exception {
        return MD5Util.getFileMD5(file);
    }

    public String uploadFileBySlices(MultipartFile slice, String fileMD5, Integer sliceNo, Integer totalSliceNo) throws IOException {
        File dbFile = fileDao.getFileByMD5(fileMD5);
        //数据库已经有这个文件了，直接返回路径（秒传功能）
        if (dbFile != null) {
            return dbFile.getUrl();
        }
        
        //上传
        String filePath = fastDFSUtil.uploadFileBySlices(slice, fileMD5, sliceNo, totalSliceNo);
        if(!StringUtils.isNullOrEmpty(filePath)){
            dbFile = new File();
            dbFile.setCreateTime(new Date());
            dbFile.setMd5(fileMD5);
            dbFile.setType(fastDFSUtil.getFileType(slice));
            dbFile.setUrl(filePath);
            fileDao.addFile(dbFile);
        }
        
        return filePath;
    }
}
