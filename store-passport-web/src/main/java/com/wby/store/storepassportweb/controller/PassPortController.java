package com.wby.store.storepassportweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wby.store.bean.UserInfo;
import com.wby.store.service.UserService;
import com.wby.util.JWTUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassPortController {
    @Reference
    UserService userService;

    String jwtKey="wby";

    @GetMapping("index.html")
    public String idnex(){
        return "index";
    }

    /**
     * 用户登录
     * @param userInfo
     * @param request
     * @return
     */
    @PostMapping("login")//查看页面下面的JS方法，
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){

        UserInfo userInfoExist=userService.login(userInfo);
        if (userInfoExist!=null){
            //制作token
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId",userInfoExist.getId());
            map.put("nickName",userInfoExist.getNickName());

            /**
             * 获取ip:
             *  1 request.getRemoteAddr()//如果用户直接访问tomcat是可以获取到ip的，但是
             *      实际上中间经过了Nginx反向代理，取到的就是Nginx的地址，所以要配置Nginx
             */
            String remoteAddr = request.getRemoteAddr();
            String ipAddr = request.getHeader("X-forwarded-for");
            String token = JWTUtil.encode(jwtKey, map, remoteAddr);
            return token;
        }
        return "fail";
    }

    /**
     * 认证服务
     * @param token
     * @param currentIp
     * @return
     */
    @GetMapping("verify")
    public String verify(@RequestParam("token") String token,
                         @RequestParam("currentIp")String currentIp){
        //1.验证token
        Map<String, Object> userMap = JWTUtil.decode(token, "wby", currentIp);
        //2.验证缓存
        if (userMap!=null){
            //2.验证缓存
            String userId = (String)userMap.get("userId");
            UserInfo userInfo=userService.verify(userId);
            if (userInfo!=null){
                return "success";
            }
        }

        return "false";

    }


}
