package com.wby.store.intemweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.wby.config.LoginRequire;
import com.wby.store.bean.SkuInfo;
import com.wby.store.bean.SpuSaleAttr;
import com.wby.store.service.ListService;
import com.wby.store.service.MangerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {
    @Reference
    MangerService mangerService;

    @Reference
    ListService listService;

    @GetMapping("{skuId}.html")
    @LoginRequire
    //@PathVariable:将someUrl/{paramId}中template 中变量，绑定到方法的参数上。
    //若方法参数名称和需要绑定的uri template中变量名称不一致，需要在@PathVariable("name")指定uri template中的名称。
    public String item(@PathVariable("skuId") String skuId, HttpServletRequest request){
        SkuInfo skuInfo =
                mangerService.getSkuINfo(skuId);
        List<SpuSaleAttr> spuSaleAttrListCheckSku =
                mangerService.getSpuSaleAttrListCheckSku(skuId, skuInfo.getSpuId());
        //String s = JSON.toJSONString(skuINfo);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("gname","<span style=\"color:green\">宝强</span>");
        request.setAttribute("spuSaleAttrListCheckSku",spuSaleAttrListCheckSku);
        //得到属性组合与skuId的映射关系，用于页面根据属性组合进行跳转
        Map skuValueIdsMap = mangerService.getSkuValueIdsMap(skuInfo.getSpuId());
        String valuesSkuJSON = JSON.toJSONString(skuValueIdsMap);
        request.setAttribute("valuesSkuJSON",valuesSkuJSON);

        listService.incrHotScore(skuId);
        request.getAttribute("userId");
        return "item";
    }
}
