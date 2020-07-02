package com.wby.store.manage.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.wby.store.bean.UserInfo;
import com.wby.store.manage.mapper.UserInfoMapper;
import com.wby.store.service.UserService;
import com.wby.store.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    // 表示当前UsernfoMapper在容器中！
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;



    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public void addUser(UserInfo userInfo) {

        // 有选择的添加
        userInfoMapper.insertSelective(userInfo);
    }

    @Override
    public void updUser(UserInfo userInfo) {
        // 根据Id 修改name
        // update userInfo set name=? where id=?
        // UserInfo userInfo1 = new UserInfo();

        userInfoMapper.updateByPrimaryKeySelective(userInfo);


    }

    @Override
    public void updUserByName(UserInfo userInfo) {
        // 根据name 修改 loginName
        // 第一个参数：userInfo 表示要修改的数据
        // 第二个参数：表示根据什么条件修改
        // update userInfo set loginName=? where name=?
        Example example = new Example(UserInfo.class);
        // 创建修改条件
        // 第一个参数：property 指的是实体类的属性名，还是数据库表中的字段名？
        // 第二个参数：修改的具体值
        example.createCriteria().andEqualTo("name",userInfo.getName());
        userInfoMapper.updateByExampleSelective(userInfo,example);


    }

//    @Override
//    public void delUser(UserInfo userInfo) {
//        // 删除数据
//        // delete from userInfo where id = ?
//        userInfoMapper.deleteByPrimaryKey(userInfo);
//    }

//    @Override
//    public void delUser(UserInfo userInfo) {
//        // 删除数据
//        // delete from userInfo where name = ?
//        // example 主要作用就是封装条件
//        Example example = new Example(UserInfo.class);
//        example.createCriteria().andEqualTo("name",userInfo.getName());
//        userInfoMapper.deleteByExample(example);
//    }


    @Override
    public void delUser(UserInfo userInfo) {
        // 删除数据
        // delete from userInfo where nickName = ?
        // example 主要作用就是封装条件
        userInfoMapper.delete(userInfo);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        //1.对比数据库，用户名密码
        String password=userInfo.getPasswd();
        String passwordMd5=
                DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(passwordMd5);

        UserInfo userInfoExists = userInfoMapper.selectOne(userInfo);
        if (userInfoExists!=null){
            //2.加载缓存
            Jedis jedis = redisUtil.getJedis();
            //type string可以单独设置过期时间 key user:101:info value  UserinfoJson
            String userKey=userKey_prefix+userInfoExists.getId()+userinfoKey_suffix;
            String userInfoJson = JSON.toJSONString(userInfoExists);
            jedis.setex(userKey,userKey_timeOut,userInfoJson);
            jedis.close();
            return userInfoExists;
        }
        return null;


    }

    @Override
    public Boolean verify(String userId) {
        //
        Jedis jedis = redisUtil.getJedis();
        String userKey=userKey_prefix+userId+userinfoKey_suffix;
        Boolean isLogin = jedis.exists(userKey);
        if (isLogin){//如果验证后，则延长用户使用时间
            jedis.expire(userKey,userKey_timeOut);
        }


        jedis.close();
        return isLogin;
    }
}
