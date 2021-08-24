package com.lhalj.emos.api.aop;

import com.lhalj.emos.api.common.utils.R;
import com.lhalj.emos.api.config.shiro.ThreadLocalToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 描述: 向客户端返回新令牌
 * 拦截所有的Web方法返回值
 *
 */
@Aspect
@Component
public class TokenAspect {


    @Autowired
    private ThreadLocalToken threadLocalToken;


    @Pointcut("execution(public * com.lhalj.emos.api.controller.*.*(..))")
    public void aspect(){

    }

    @Around("aspect()")
    public Object around(ProceedingJoinPoint point) throws Throwable{
        R r = (R) point.proceed();
        //获取token令牌
        String token = threadLocalToken.getToken();
        //如果ThreadLocal中存在token 则说明是更新的Token
        if (token!=null) {
            r.put("token",token);//往响应中放token
            //清除老数据
            threadLocalToken.clear();
        }
    return r;
    }


}
