package com.oymn.bilibili.utils;

import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.mysql.cj.util.StringUtils;
import com.oymn.bilibili.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

@Component
public class FastDFSUtil {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;    //用于上传文件分片

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String DEFAULT_GROUP = "group1";

    private static final String PATH_KEY = "path:";   //用于记录上传的文件的路径

    private static final String UPLOADED_SIZE_KEY = "uploaded-size:";   //用于记录已经上传文件的大小

    private static final String UPLOADED_NO_KEY = "uploaded-no:";   //用于记录已经上传文件的片数

    private static final int SLICE_SIZE = 1024 * 1024 * 2;    //每片为2M
    
    @Value("${fdfs.http.storage-addr}")
    private String httpFdfsStorageAddr;

    //上传文件
    public String uploadCommonFile(MultipartFile file) throws IOException {
        Set<MetaData> metaDataSet = new HashSet<>();
        String fileType = getFileType(file);
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), fileType, metaDataSet);
        return storePath.getPath();
    }

    //上传断点续传的文件（第一片）
    public String uploadAppenderFile(MultipartFile file) throws IOException {
        String fileType = this.getFileType(file);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(DEFAULT_GROUP, file.getInputStream(), file.getSize(), fileType);
        return storePath.getPath();
    }

    //上传后续的文件片
    public void modifyAppenderFile(MultipartFile file, String filePath, long offset) throws IOException {
        appendFileStorageClient.modifyFile(DEFAULT_GROUP, filePath, file.getInputStream(), file.getSize(), offset);
    }

    //上传可以断点续传的文件
    public String uploadFileBySlices(MultipartFile file, String fileMd5, Integer sliceNo, Integer totalSileNo) throws IOException {
        if (file == null || sliceNo == null || totalSileNo == null) {
            throw new ConditionException("参数异常！");
        }

        String pathKey = PATH_KEY + fileMd5;    //用于记录文件路径
        String uploadedSizeKey = UPLOADED_SIZE_KEY + fileMd5;   //用于记录已经上传文件的大小
        String uploadedNoKey = UPLOADED_NO_KEY + fileMd5;   //用于记录已经上传文件的片号

        //获取之前已经上传过的文件大小
        String uploadedSizeStr = redisTemplate.opsForValue().get(uploadedSizeKey);
        Long uploadedSize = 0L;
        if (!StringUtils.isNullOrEmpty(uploadedSizeStr)) {
            uploadedSize += Long.valueOf(uploadedSizeStr);
        }

        //此时上传的是该文件的第一片
        if (sliceNo == 1) {
            String path = this.uploadAppenderFile(file);
            if (StringUtils.isNullOrEmpty(path)) {
                throw new ConditionException("文件上传失败！");
            }

            redisTemplate.opsForValue().set(pathKey, path);
            redisTemplate.opsForValue().set(uploadedNoKey, "1");
        } else {
            //此时上传的是该文件后续的片段
            String path = redisTemplate.opsForValue().get(pathKey);
            if (StringUtils.isNullOrEmpty(path)) {
                throw new ConditionException("文件上传失败！");
            }
            //上传
            this.modifyAppenderFile(file, path, uploadedSize);
            redisTemplate.opsForValue().increment(uploadedNoKey);
        }

        //修改已上传文件的大小
        uploadedSize += file.getSize();
        redisTemplate.opsForValue().set(uploadedSizeKey, String.valueOf(uploadedSize));

        //如果所有分片都已经上传完毕，则清空redis里面相关的key和value
        String uploadedNoStr = redisTemplate.opsForValue().get(uploadedNoKey);
        Integer uploadedNo = Integer.valueOf(uploadedNoStr);
        String resultPath = "";
        if (totalSileNo.equals(uploadedNo)) {
            resultPath = redisTemplate.opsForValue().get(pathKey);
            //删除相关的key
            List<String> keyList = Arrays.asList(pathKey, uploadedNoKey, uploadedSizeKey);
            redisTemplate.delete(keyList);
        }

        return resultPath;
    }

    //实现文件分片功能（此功能一般由前端完成，此处只为学习使用）
    public void convertFileToSlices(MultipartFile multipartFile) throws IOException {
        String fileType = this.getFileType(multipartFile);

        //生成临时文件，将MultipartFile转换为File
        File file = this.multipartFileToFile(multipartFile);

        long fileLength = file.length();
        int count = 1;
        for (int i = 0; i < fileLength; i += SLICE_SIZE) {
            //随机访问文件的类
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            //定位到偏移量为i的位置
            randomAccessFile.seek(i);
            byte[] bytes = new byte[SLICE_SIZE];
            int len = randomAccessFile.read(bytes);

            //将分片保存到以下路径中
            String path = "D://" + count + "." + fileType;
            File slice = new File(path);
            FileOutputStream fos = new FileOutputStream(slice);
            fos.write(bytes, 0, len);
            fos.close();
            
            randomAccessFile.close();
            count++;
        }
        
        //删除临时文件
        file.delete();
    }

    //将MultipartFile转换为File
    public File multipartFileToFile(MultipartFile multipartFile) throws IOException {
        //获取文件名
        String originalFileName = multipartFile.getOriginalFilename();
        String[] fileName = originalFileName.split("\\.");
        File file = File.createTempFile(fileName[0], "." + fileName[1]);
        //将MultipartFile转换为File
        multipartFile.transferTo(file);
        return file;
    }

    //删除文件
    public void deleteFile(String filePath) {
        fastFileStorageClient.deleteFile(filePath);
    }

    //获取文件类型
    public String getFileType(MultipartFile file) {

        if (file == null) {
            throw new ConditionException("非法文件！");
        }

        String fileName = file.getOriginalFilename();
        int index = fileName.lastIndexOf(".");
        return fileName.substring(index + 1);
    }

    //在线播放视频
    public void viewVideoOnlineBySlices(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        String path) throws Exception {
        //获取该路径对应的文件的信息
        FileInfo fileInfo = fastFileStorageClient.queryFileInfo(DEFAULT_GROUP, path);
        long fileSize = fileInfo.getFileSize();
        String url = httpFdfsStorageAddr + path;   //path是相对路径

        //将请求头全部信息转发到DFS服务器
        Enumeration<String> headerNames = request.getHeaderNames();
        //获取请求头
        Map<String, Object> headers = new HashMap<>();
        while(headerNames.hasMoreElements()){
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }

        //获取请求头中Range键值对（用于标识这个文件片的偏移量范围）
        //该键值对的格式为 bytes=xxx-xxx
        String rangeStr = request.getHeader("Range");
        String[] range;
        if(StringUtils.isNullOrEmpty(rangeStr)){
            //设置为整个文件大小
            rangeStr = "bytes=0-" + (fileSize - 1);
        }
        
        range = rangeStr.split("bytes=|-");
        long begin = 0;
        if(range.length >= 2){
            begin = Long.parseLong(range[1]);
        }
        long end = fileSize - 1;
        if(range.length >= 3){
            end = Long.parseLong(range[2]);
        }
        
        long len = end - begin;
        //给响应头设置Content-Range键值对
        //该键值对的格式为：bytes xxx-xxx/xxx(总的文件大小) 
        String contentRange = "bytes " + begin + "-" + end + "/" + fileSize;
        response.setHeader("Content-Range", contentRange);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", "video/mp4");
        response.setContentLength((int) len);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        
        //将请求转发到DFS服务器
        HttpUtil.get(url, headers, response);
    }
}
