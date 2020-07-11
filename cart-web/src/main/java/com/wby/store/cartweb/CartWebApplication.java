package com.wby.store.cartweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.wby.store")
public class CartWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartWebApplication.class, args);
    }

}
