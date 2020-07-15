package com.wby.store.cartservice.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.wby.store.bean.CartInfo;
import com.wby.store.bean.SkuInfo;
import com.wby.store.cartservice.service.mapper.CartInfoMapper;
import com.wby.store.service.CartService;
import com.wby.store.service.MangerService;
import com.wby.store.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    MangerService mangerService;

    @Override
    public CartInfo addCart(String userId, String skuId, Integer num) {
        /**
         * 加数据库
         * 1.尝试去除已有的数据，如果有，更新；没有则插入。
         */
        CartInfo cartInfoQuery=new CartInfo();
        cartInfoQuery.setUserId(userId);
        cartInfoQuery.setSkuId(skuId);
        CartInfo cartInfoExist=null;
        cartInfoExist = cartInfoMapper.selectOne(cartInfoQuery);
        SkuInfo skuINfo = mangerService.getSkuINfo(skuId);
        if (cartInfoExist!=null){
            cartInfoExist.setSkuName(skuINfo.getSkuName());
            cartInfoExist.setCartPrice(skuINfo.getPrice());
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+num);
            cartInfoExist.setImgUrl(skuINfo.getSkuDefaultImg());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        }else {
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(num);
            cartInfo.setImgUrl(skuINfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuINfo.getSkuName());
            cartInfo.setCartPrice(skuINfo.getPrice());
            cartInfo.setSkuPrice(skuINfo.getPrice());
            cartInfoMapper.insertSelective(cartInfo);
            cartInfoExist=cartInfo;
        }


        //加缓存.type(hash) key(cart:userId:info) field(skuId) value(cartInfoJson)
        //还需要考虑是新增还是修改。如果购物车中已有sku，则增加个数，否则是新增一条
        Jedis jedis = redisUtil.getJedis();
        String cartKey="cart:"+userId+":info";
        String cartInfoJson = JSON.toJSONString(cartInfoExist);
        jedis.hset(cartKey,skuId,cartInfoJson);//新增。同时也可以覆盖
        jedis.close();

        return cartInfoExist;
    }

    @Override
    public List<CartInfo> cartList(String userId) {
        //先查缓存
        Jedis jedis = redisUtil.getJedis();
        String cartKey="cart:"+userId+":info";

        List<String> cartJSONList = jedis.hvals(cartKey);
        List<CartInfo> cartInfoList=new ArrayList<>();
        if (cartJSONList!=null&&cartJSONList.size()>0){//命中缓存
            for (String cartJSON:cartJSONList
             ) {
                CartInfo cartInfo = JSON.parseObject(cartJSON, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o2.getId().compareTo(o1.getId());

                }
            });
            //cartInfoList.sort((o1, o2) -> -1);
            return cartInfoList;
        }else {//缓存未命中,同时加载到缓存中
           return loadCartCache(userId);
        }



    }

    /**
     * 缓存未命中,同时加载到缓存中
     * @param userId
     * @return
     */
    public List<CartInfo> loadCartCache(String userId){
        /*//读取数据库
        CartInfo cartInfo=new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfoList = cartInfoMapper.select(cartInfo);
        //写入缓存
        for (CartInfo cartInfo1 :cartInfoList
             ) {
            List<CartInfo> cartInfoList1 =
                    cartInfoMapper.selectCartListWithSkuPrice(userId);

        }*/


        //读取数据库
        List<CartInfo> cartInfoList =
                cartInfoMapper.selectCartListWithSkuPrice(userId);
        //写入缓存
        Jedis jedis = redisUtil.getJedis();
            //方便插入，把list转换为map
        Map<String,String > cartMap=new HashMap<>();
        for (CartInfo cartInfo:cartInfoList
             ) {
            cartMap.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        String cartKey="cart:"+userId+":info";
        jedis.hmset(cartKey,cartMap);
        jedis.expire(cartKey,60*60*24);


        return cartInfoList;
    }
}
