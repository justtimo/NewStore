package com.wby.store.mangerweb.comtroller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wby.store.bean.SkuInfo;
import com.wby.store.service.MangerService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class SkuController {
    @Reference
    MangerService mangerService;

    @PostMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo){
        mangerService.saveSkuInfo(skuInfo);
        return "ok";
    }
}
