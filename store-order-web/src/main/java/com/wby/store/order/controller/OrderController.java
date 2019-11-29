package com.wby.store.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wby.store.bean.UserInfo;
import com.wby.store.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {
    //@Autowired 这里不能用这个注解，因为他是在自己的容器中找的，也就是order-web这个模块中，
    //此处需要使用dubbo
    @Reference
    UserService userService;

    @GetMapping("trade")
    public List<UserInfo> trade(){
        List<UserInfo> all = userService.findAll();
        return all;
    }
}
