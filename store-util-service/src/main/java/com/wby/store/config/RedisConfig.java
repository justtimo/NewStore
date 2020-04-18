package com.wby.store.config;

import com.wby.store.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 如果每个模块的Redis地址不一样，那么要将配置文件分别放到对应模块中
 */
@Configuration
public class RedisConfig {
    //@Value:读取配置文件中的redis的ip地址
    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    @Bean
    public RedisUtil getRedisUtil(){
        //做判断，如果配置文件中没有对应属性，就不创建连接池
        if(host.equals("disabled")){
            return null;
        }
        RedisUtil redisUtil=new RedisUtil();
        redisUtil.initJedisPool(host,port,database);
        return redisUtil;
    }

}
