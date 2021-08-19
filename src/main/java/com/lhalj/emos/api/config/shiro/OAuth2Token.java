package com.lhalj.emos.api.config.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * 描述: 把令牌封装成认证对象 方便传递到shiro
 */
public class OAuth2Token implements AuthenticationToken {

    private String token;

    public OAuth2Token(String token) {
        this.token = token;
    }


    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
