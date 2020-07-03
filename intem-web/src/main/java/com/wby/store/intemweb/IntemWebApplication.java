package com.wby.store.intemweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.wby.store")
public class IntemWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntemWebApplication.class, args);
    }

}
