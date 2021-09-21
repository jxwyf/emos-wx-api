package com.lhalj.emos.api.config.shiro;

import com.lhalj.emos.api.db.pojo.TbUser;
import com.lhalj.emos.api.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 描述: 实现认证授权
 */
@Component
public class OAuth2Realm extends AuthorizingRealm {


    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    //判断传入的令牌封装对象是否符合要求
    @Override
    public boolean supports(AuthenticationToken token){
        return token instanceof OAuth2Token;
    }


    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection collection) {
        TbUser user = (TbUser) collection.getPrimaryPrincipal();
        int userId = user.getId();
        //用户权限列表
        Set<String> permsSet = userService.searchUserPermissions(userId);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 把权限列表添加到info对象中
        info.setStringPermissions(permsSet);

        return info;
    }

    //认证 登录时调用
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
       //TODO 从令牌中获取userId 然后检测用户是否冻结
        String accessToken = (String) token.getPrincipal();
        int userId = jwtUtil.getUserId(accessToken);
        //查询用户信息
        TbUser user = userService.searchById(userId);
        if (user==null) {
            throw new LockedAccountException("账号已被锁定,请联系管理员");
        }
        // 往info对象添加用户消息 token字符串 getName() 获得Realm类的名字
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user,accessToken,getName());
        return info;
    }
}
