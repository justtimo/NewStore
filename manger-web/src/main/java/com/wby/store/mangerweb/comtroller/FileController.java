package com.wby.store.mangerweb.comtroller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传
 */
@RestController
@CrossOrigin
public class FileController {
    @Value("${fileServer.url}")
    String fileServerUrl;

    @PostMapping("fileUpload")
    public String uploadFile(@RequestParam("file")MultipartFile file) throws IOException, MyException {
        //1.读取tracker配置文件,file是一个绝对路径
        String confFile = this.getClass().getResource("/tracker.conf").getFile();
        //2.ClientGlobal可以吧tracker.conf文件中的内容加载到环境中，也就是内存
        ClientGlobal.init(confFile);
        //3.TrackerClient，客户端连接服务器
        TrackerClient trackerClient=new TrackerClient();
        TrackerServer trackerServer=trackerClient.getConnection();
        //4.获得StorageClient
        StorageClient storageClient=new StorageClient(trackerServer,null);
        //5.上传文件，返回一个文件的路径地址数组
            //5.1
        byte[] bytes = file.getBytes();
            //5.2
        String originalFilename = file.getOriginalFilename();//原始文件名
        String s1 = StringUtils.substringAfterLast(originalFilename, ".");
        String[] upload_file = storageClient.upload_file(bytes, s1, null);
        //6.打印路径地址数组
        String fileUrl=fileServerUrl;
        for (int i = 0; i < upload_file.length; i++) {
            String s = upload_file[i];
            fileUrl+="/"+s;
        }
        return  fileUrl;
    }
}
