package com.wby.store.storepassportweb;


import com.alibaba.fastjson.JSON;
import com.wby.util.JWTUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class StorePassportWebApplicationTests {

    //JWTUtil jwtUtil;

    @Test
    public void contextLoads() {
    }

    @Test
    public void test1(){
        String key = "atguigu";
        String ip="192.168.67.201";
        Map map = new HashMap();
        map.put("userId","1001");
        map.put("nickName","marry");
        String token = JWTUtil.encode(key, map, ip);
        System.out.println("token:::"+token);
        Map<String, Object> decode = JWTUtil.decode(token, key, "192.168.67.201");
        String s = JSON.toJSONString(decode);
        System.out.println("decode:::"+decode);
    }

}
