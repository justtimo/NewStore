package com.wby.interceptor;
import com.alibaba.fastjson.JSON;
import com.wby.config.LoginRequire;
import com.wby.constants.WebConst;
import com.wby.util.CookieUtil;
import com.wby.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandle;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.wby.constants.WebConst.VERIFY_URL;

/**
 * 拦截器
 */
@Component
public class AuthInterceptor  extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token=null;
        //一.检查token。可能存在：
        // 1.url参数中  newToken
        // 2.从cookie中获得，token
        //针对1的情况，newToken
        request.getParameter("newToken");
        if (token!=null){
            //吧token保存到cookie中
            CookieUtil.setCookie(request,response,"token",
                    token, WebConst.cookieMaxAge,false);
        }else {
            //没有token，可能没登陆，可能在cookie中
            // from cookie
            token = CookieUtil.getCookieValue(request, "token", false);

        }


        //如果token有，从yoken中把用户信息取出来
        Map userMap=new HashMap();
        if (token!=null){
            userMap = getUserMapFromToken(token);
            String nickName = (String)userMap.get("nickName");
            request.setAttribute("nickName",nickName);
        }


        //判断是否该请求需要用户登陆
        //取到请求的方法上的注解，是否有LoginRequire。
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire loginRequire =
                handlerMethod.getMethodAnnotation(LoginRequire.class);

        if (loginRequire!=null){
            //需要认证
            if (token!=null){
                //吧token发给认证中心认证
                //String currentIp = request.getHeader("X-forwarded-for");生产黄静使用
                String currentIp ="127.0.0.1";//本地测试使用

                String result =
                        HttpClientUtil.doGet(VERIFY_URL + "?token=" + token + "&currentIp=" + currentIp);
                if ("success".equals(result)){
                    String userId =(String) userMap.get("userId");
                    request.setAttribute("userId",userId);

                    return true;
                }else if (!loginRequire.autoRedirect()){//认证失败，但是允许不跳转
                    return true;
                }else {//认证失败，强行跳转
                    redirect(request,response);
                    return false;

                }
            }else{
                //重定向到passport让用户进行登陆
                redirect(request,response);
                return false;
            }
        }





        return true;
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String  requestURL = request.getRequestURL().toString();
        String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
        response.sendRedirect(WebConst.LOGIN_URL+"?originUrl="+encodeURL);

    }

    private Map getUserMapFromToken(String token){
        //xxxxasd.qwert.zxcvzx截取中间的部分，利用base64解码得到json串
        String userBase64=StringUtils.substringBetween(token,".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] userBytes = base64UrlCodec.decode(userBase64);
        String userJson = null;
        try {
            userJson = new String(userBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map userMap = JSON.parseObject(userJson, Map.class);
        return userMap;

    }

}
