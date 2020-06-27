package com.wby.store.managerservice.mapper;

import com.wby.store.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    public List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);

    //根据搜索出来的商品的平台属性值（多个）  查询出对应的 平台属性+属性值清单列表
    //这里加@Param("valueIds")是因为mapper中使用了${}的原因
    public List<BaseAttrInfo> getBaseAttrInfoListByValueIds(@Param("valueIds") String valueIds);

}
