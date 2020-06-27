package com.wby.store.storelistservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.wby.store")
public class StoreListServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreListServiceApplication.class, args);
    }

}
