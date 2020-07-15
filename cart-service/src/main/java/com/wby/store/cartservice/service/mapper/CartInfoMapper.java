package com.wby.store.cartservice.service.mapper;

import com.wby.store.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {
        public List<CartInfo> selectCartListWithSkuPrice(String userId);
}
