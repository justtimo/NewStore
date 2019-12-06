package com.wby.store.mangerweb;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
class MangerWebApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void uploadFile() throws IOException, MyException {
        //1.读取tracker配置文件,file是一个绝对路径
        String file = this.getClass().getResource("/tracker.conf").getFile();
        //2.ClientGlobal可以吧tracker.conf文件中的内容加载到环境中，也就是内存
        ClientGlobal.init(file);
        //3.TrackerClient，客户端连接服务器
        TrackerClient trackerClient=new TrackerClient();
        TrackerServer trackerServer=trackerClient.getConnection();
        //4.获得StorageClient
        StorageClient storageClient=new StorageClient(trackerServer,null);
        //5.上传文件，返回一个文件的路径地址数组
        String orginalFilename="C://Users//Administrator//Desktop//123.png";
        String[] upload_file = storageClient.upload_file(orginalFilename, "png", null);
        //6.打印路径地址数组
        for (int i = 0; i < upload_file.length; i++) {
            String s = upload_file[i];
            System.out.println("s = " + s);
            System.out.println(upload_file.length);
        }


    }

}
