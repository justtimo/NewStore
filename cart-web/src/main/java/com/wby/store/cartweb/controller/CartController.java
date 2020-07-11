package com.wby.store.cartweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wby.config.LoginRequire;
import com.wby.store.bean.CartInfo;
import com.wby.store.service.CartService;
import com.wby.util.CookieUtil;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sun.java2d.pipe.AAShapePipe;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {
    @Reference
    CartService cartService;


    @PostMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addCart(@RequestParam("skuId") String skuId, @RequestParam("num")int num,
                          HttpServletRequest request, HttpServletResponse response){
        String userId = (String)request.getAttribute("userId");
       if (userId==null){
            //如果未登录。每次往购物车添加商品的时候，看看cookie中是不是已经有token；
            // 如果有token，使用token座位Id加购物车；
            // 如果没有token，则生成一个新的token放入cookie；
           userId =
                    CookieUtil.getCookieValue(request, "user_tem_id", false);
            if (userId==null){
                userId= UUID.randomUUID().toString();
                CookieUtil.setCookie(request,response,"user_tem_id",userId,60*60*24*7,false);
            }

        }
        CartInfo cartInfo = cartService.addCart(userId, skuId, num);
       request.setAttribute("cartInfo",cartInfo);
        request.setAttribute("num",num);

        return "success";
    }

    @GetMapping("cartList")
    @LoginRequire
    public String cartList(HttpServletRequest request){
        String userId = (String)request.getAttribute("userId");
        if (userId==null){
            userId =
                    CookieUtil.getCookieValue(request, "user_tem_id", false);

        }
        if (userId==null){
            return "cartList";
        }else {
            List<CartInfo> cartInfoList = cartService.cartList(userId);
        }

        return null;
    }

}
