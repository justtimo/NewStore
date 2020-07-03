package com.wby.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


//修饰注解是注在哪的
@Target(ElementType.METHOD)
//运行级别：source级别：
// 比如override。编译成class注解他就没了
// class级别:编译成clas还有，运行时没了
// runtime级别:注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在

/**
 * 为了区别认证的时候哪些方法必须登录
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequire {
    //定义的注解中的参数。比如这个自定义的注解是登陆，当没有登陆时，做相应的处理：重定向。
    //ture：强行跳转 false：不强行跳转
    boolean autoRedirect() default true;

}
