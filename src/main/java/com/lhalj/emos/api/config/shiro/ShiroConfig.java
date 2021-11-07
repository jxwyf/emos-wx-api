package com.lhalj.emos.api.config.shiro;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 描述: 把Filter和Realm添加到Shiro框架
 * 创建四个对象返回给SpringBoot
 */
@Configuration
public class ShiroConfig {

    /*
     * 用于封装Realm对象
     */
    @Bean("securityManager")
    public SecurityManager securityManager(OAuth2Realm oAuth2Realm){
        //SecurityManager 子类对象
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(oAuth2Realm);
        securityManager.setRememberMeManager(null);
        return securityManager;
    }

    /*
     * 用于封装Filter对象
     * 设置Filter拦截路径
     */
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager, OAuth2Filter filter){
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        Map<String, Filter> map = new HashMap<>();
        map.put("oauth2",filter);
        shiroFilter.setFilters(map);



        //需要拦截的请求
        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/webjars/**","anon");
        filterMap.put("/druid/**","anon");
        filterMap.put("/app/**","anon");
        filterMap.put("/sys/login","anon");
        filterMap.put("/swagger/**","anon");
        filterMap.put("/v2/api-docs","anon");
        filterMap.put("/swagger-ui.html","anon");
        filterMap.put("/swagger-resources/**","anon");
        filterMap.put("/captcha.jpg","anon");
        filterMap.put("/user/register","anon");
        //TODO
//        filterMap.put("/meeting/searchMyMeetingListByPage1","anon");
        filterMap.put("/user/login","anon");
        filterMap.put("/test/**","anon");
        //调用上面的拦截方法
        filterMap.put("/**","oauth2");
        shiroFilter.setFilterChainDefinitionMap(filterMap);

        return shiroFilter;
    }

    /*
     * 管理Shiro对象生命周期
     */
    @Bean("lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }

    /*
     * AOP切面类
     * web方法执行前 验证权限
     */
    @Bean("authorizationAttributeSourceAdvisor")
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager){
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }



}
