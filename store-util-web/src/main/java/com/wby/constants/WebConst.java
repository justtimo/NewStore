package com.wby.constants;

/**
 * 常量类
 */
public class WebConst {
    //登录页面
    //public final static String LOGIN_URL="http://passport.wubingyin.cn/index";生产环境
    public final static String LOGIN_URL="http://localhost:8087/index";
    //认证接口
    //public final static String VERIFY_URL="http://passport.wubingyin.cn/verify";
    public final static String VERIFY_URL="http://localhost:8087/verify";
    //cookie的有效时间：默认给7天
    public final static int cookieMaxAge=7*24*3600;
}
