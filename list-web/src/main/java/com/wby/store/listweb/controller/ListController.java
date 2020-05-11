package com.wby.store.listweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.wby.store.bean.SkuLsParams;
import com.wby.store.bean.SkuLsResult;
import com.wby.store.service.ListService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ListController {

    @Reference
    ListService listService;

    @GetMapping("list.html")
    @ResponseBody
    public String list(SkuLsParams skuLsParams){
        SkuLsResult skuLsResult = listService.getSkuLsInfoList(skuLsParams);

        return JSON.toJSONString(skuLsResult);
    }
}
