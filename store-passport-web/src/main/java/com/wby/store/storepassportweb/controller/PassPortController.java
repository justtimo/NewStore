package com.wby.store.storepassportweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PassPortController {

    @GetMapping("index.html")
    public String idnex(){
        return "index";
    }
}
