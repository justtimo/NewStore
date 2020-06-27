package com.wby.store.managerservice.mapper;

import com.wby.store.bean.SkuAttrValue;
import com.wby.store.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 平台属性zhi
 */
public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    public List<Map> getSaleAttrValuesBySpu(String spuId);
}
