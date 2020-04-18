package com.wby.store.mangerweb.comtroller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wby.store.bean.*;
import com.wby.store.service.MangerService;
import org.apache.ibatis.annotations.Options;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;

@RestController
@CrossOrigin        //解决跨域问题
public class ManagerController {

    @Reference
    MangerService mangerService;

    @PostMapping("getCatalog1")
    public List<BaseCatalog1> getBaseCatalog1List(){
        return  mangerService.getCatalog1();
    }

    @PostMapping("getCatalog2")
    public List<BaseCatalog2> getBaseCatalog2List(String catalog1Id){
        return  mangerService.getCatalog2(catalog1Id);
    }

    @PostMapping("getCatalog3")
    public List<BaseCatalog3> getBaseCatalog3List(String catalog2Id){
        return  mangerService.getCatalog3(catalog2Id);
    }

    @GetMapping("attrInfoList")
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id){
        return  mangerService.getAttrList(catalog3Id);
    }

    @PostMapping("saveAttrInfo")
    public String saveInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        mangerService.saveBaseAttrInfo(baseAttrInfo);
        return "success";
    }

    @PostMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){
        BaseAttrInfo attrInfo = mangerService.getAttrInfo(attrId);
        List<BaseAttrValue> attrValueList = attrInfo.getAttrValueList();
        return attrValueList;
    }


    @GetMapping("spuList")
    public List<SpuInfo> getSpuList(String catalog3Id){
        List<SpuInfo> spuList = mangerService.getSpuList(catalog3Id);
        return  spuList;
    }

    @PostMapping("baseSaleAttrList")
    public  List<BaseSaleAttr> getBaseSaleAttrList(){
        return mangerService.getBaseSaleAttrList();
    }

    @PostMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo){
        mangerService.saveSpuInfo(spuInfo);
        return "ok";
    }

    @GetMapping("spuImageList")
    public List<SpuImage> getSpuImageList(String spuId){
        return mangerService.getSpuImageList(spuId);
    }

    @GetMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId){
        return  mangerService.getSpuSaleAttrList(spuId);
    }


}
