package com.wby.store.service;

import com.wby.store.bean.*;

import java.util.List;

public interface MangerService{
    /**
     * 查询一级分类
     */
    public List<BaseCatalog1> getCatalog1();

    /**
     * 查询二级分类，根据一级分类ID查询
     */
    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 查询三级分类，根据二级分类ID查询
     */
    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 根据三级分类查询平台属性

     * 删除平台属性
     */
    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    /**
     * 保存平台属性
     * @param baseAttrInfo
     */
    public  void saveBaseAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据平台属性id查询平台属性详情，顺便吧该属性值的属性列表也取出来
     */
    public BaseAttrInfo getAttrInfo(String attrId);

    /**
     * 商品spu列表查询
     */
    List<SpuInfo> getSpuList(String catalog3Id);

    /**
     * 查询基本销售属性表
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存spu商品信息
     * @param spuInfo
     */
    public void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 初始化sku页面，获取图片列表
     * @param spuId
     * @return
     */
    public List<SpuImage> getSpuImageList(String spuId);

}
