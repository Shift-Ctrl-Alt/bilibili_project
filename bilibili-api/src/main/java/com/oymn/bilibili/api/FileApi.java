package com.oymn.bilibili.api;

import com.oymn.bilibili.domain.JsonResponse;
import com.oymn.bilibili.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
public class FileApi {
    
    @Autowired
    private FileService fileService;
    
    //将文件片转换为md5加密字符串
    @PostMapping("/md5files")
    public JsonResponse<String> getFileMD5(MultipartFile file) throws Exception {
        String fileMD5 = fileService.getFileMD5(file);
        return new JsonResponse<>(fileMD5);
    }
    
    //上传文件
    @PutMapping("/file-slices")
    public JsonResponse<String> uploadFileBySlices(MultipartFile file,
                                                   String fileMD5,
                                                   Integer sliceNo,
                                                   Integer totalSliceNo) throws IOException {
        String filePath = fileService.uploadFileBySlices(file, fileMD5, sliceNo, totalSliceNo);
        return new JsonResponse<>(filePath);
    }
}
