package com.wby.store.managerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.wby.store.managerservice.mapper")
@EnableTransactionManagement    //在serviceImpl中添加了事务，这里也要开启事务
public class ManagerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManagerServiceApplication.class, args);
    }

}
