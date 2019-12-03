package com.wby.store.managerservice.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.wby.store.bean.*;
import com.wby.store.managerservice.mapper.*;
import com.wby.store.service.MangerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
@Service
public class ManagerServiceImpl implements MangerService {
    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        Example example=new Example(BaseCatalog2.class);
        //andEqualTo()第一个参数是实体类中的属性(property)，而不是数据库中的字段名,如果是数据库中的话应该是
        //column或者filed
        example.createCriteria().andEqualTo("catalog1Id",catalog1Id);
        return baseCatalog2Mapper.selectByExample(example);
       /* BaseCatalog2 baseCatalog2=new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> select = baseCatalog2Mapper.select(baseCatalog2);
        return select;*/
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        Example example=new Example(BaseCatalog3.class);
        example.createCriteria().andEqualTo("catalog2Id",catalog2Id);
        return baseCatalog3Mapper.selectByExample(example);
        /*BaseCatalog3 baseCatalog3=new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> select = baseCatalog3Mapper.select(baseCatalog3);
        return select;*/
    }


    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        Example example=new Example(BaseAttrInfo.class);
        example.createCriteria().andEqualTo("catalog3Id",catalog3Id);
        return baseAttrInfoMapper.selectByExample(example);
    }

    @Override
    @Transactional  //涉及多表，添加事务
    public void saveBaseAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId()!=null&&baseAttrInfo.getId().length()>0){
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else {
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //根据attrId先全部删除属性值，在统一保存
        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId",baseAttrInfo.getId());
        baseAttrValueMapper.deleteByExample(example);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            String id = baseAttrInfo.getId();
            baseAttrValue.setAttrId(id);
            baseAttrValueMapper.insertSelective(baseAttrValue);
        }
    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        Example example=new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId",attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectByExample(example);
        baseAttrInfo.setAttrValueList(baseAttrValueList);
        return baseAttrInfo;
    }


}
