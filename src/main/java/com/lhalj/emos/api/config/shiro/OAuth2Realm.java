package com.lhalj.emos.api.config.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 描述: 实现认证授权
 */
@Component
public class OAuth2Realm extends AuthorizingRealm {


    @Autowired
    private JwtUtil jwtUtil;

    //判断传入的令牌封装对象是否符合要求
    @Override
    public boolean supports(AuthenticationToken token){
        return token instanceof OAuth2Token;
    }


    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        //TODO 查询用户的权限列表
        //TODO 把权限列表添加到info对象中
        return info;
    }

    //认证 登录时调用
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
       //TODO 从令牌中获取userId 然后检测用户是否冻结
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo();
        //TODO 往info对象添加用户消息 token字符串
        return info;
    }
}
