package com.wby.store.service;

import com.wby.store.bean.CartInfo;

import java.util.List;

public interface CartService {

    public CartInfo addCart(String userId, String skuId, Integer num);

    public List<CartInfo> cartList(String userId);
}
