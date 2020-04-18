package com.wby.store.managerservice.mapper;

import com.wby.store.bean.BaseAttrInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    public List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);
}
