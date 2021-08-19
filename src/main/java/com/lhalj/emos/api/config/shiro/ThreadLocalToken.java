package com.lhalj.emos.api.config.shiro;

import org.springframework.stereotype.Component;

/**
 * 描述:
 */
@Component
public class ThreadLocalToken {

    private ThreadLocal local = new ThreadLocal();

    //设置
    public void setToken(String token){
        local.set(token);
    }

    //取值
    public String getToken(){
        return (String) local.get();
    }

    //清除
    public void clear(){
        local.remove();
    }
}
