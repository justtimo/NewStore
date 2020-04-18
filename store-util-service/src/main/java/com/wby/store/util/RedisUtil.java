package com.wby.store.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

    private JedisPool jedisPool;

    public  void  initJedisPool(String host,int port,int database){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 总数
        jedisPoolConfig.setMaxTotal(200);
        // 获取连接时等待的最大毫秒
        jedisPoolConfig.setMaxWaitMillis(10*1000);
        // 最少剩余数
        jedisPoolConfig.setMinIdle(10);
        //最多连接数
        //jedisPoolConfig.setMaxIdle(20);
        // 如果到最大数，设置等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        // 在获取连接时，检查是否有效.有两端，Tomcat连接池和Redis连接池，如果Tomcat连接池中的链接没了
        jedisPoolConfig.setTestOnBorrow(true);
        // 创建连接池。2*1000超时时间
        jedisPool = new JedisPool(jedisPoolConfig,host,port,2*1000);

    }
    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
