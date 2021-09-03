package com.lhalj.emos.api.config.shiro;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 描述:SpringMVC过滤器
 * 2 第二
 * 普通的bean
 */
@Component
@Scope("prototype")
public class OAuth2Filter extends AuthenticatingFilter {

    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate redisTemplate;




    //创建令牌
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        //强制转换
        HttpServletRequest req = (HttpServletRequest) request;
        String token = getRequestToken(req);
        if (StrUtil.isBlank(token)) {
          return null;
        }
        // 不为空 封装
        return new OAuth2Token(token);
    }

    //判断哪一种请求可以被shiro管理
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        //数据类型转换
        HttpServletRequest req = (HttpServletRequest) request;
        //如果请求类型是OPTIONS  不被shiro管理
        if (req.getMethod().equals(RequestMethod.OPTIONS.name())) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        //数据类型转换
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType("text/html");
        //响应字符集
        resp.setCharacterEncoding("UTF-8");
        //往响应头里设置跨域的参数
        resp.setHeader("Access-Control-Allow-Credentials","true");
        resp.setHeader("Access-Control-Allow-Origin",req.getHeader("Origin"));
        //清空数据
        threadLocalToken.clear();
        //从请求头里获得token字符串
        String token = getRequestToken(req);
        //判断令牌是否为空
        if (StrUtil.isBlank(token)) {
            //设置响应状态码
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌");
            //不通过 不需要shiro来进行授权认证 直接打回
            return false;
        }
        //验证令牌
        try {
            jwtUtil.verifierToken(token);
        } catch (TokenExpiredException e) {
            //刷新令牌
            if (redisTemplate.hasKey(token)) {
                //删除老令牌
                redisTemplate.delete(token);
                //获得用户id
                int userId = jwtUtil.getUserId(token);
                //生成新令牌
                token = jwtUtil.createToken(userId);
                // cacheExpire 过期时间 天
                redisTemplate.opsForValue().set(token, userId + "",cacheExpire, TimeUnit.DAYS);
                threadLocalToken.setToken(token);

            }
            //客户端令牌过期了 redis中没有令牌
            else {
                //设置响应状态码
                resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
                resp.getWriter().print("令牌过期");
                return false;
            }
        } catch (JWTDecodeException e){
            //设置响应状态码
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌");
            return false;
        }
        //请求和响应 间接的让shiro执行realm类
        boolean bool = executeLogin(request, response);
        return bool;
    }

    //shiro 在执行relam方法时候判断有没有登录或者登录失败
    //认证失败会执行下面的方法
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        //数据类型转换
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType("text/html");
        //响应字符集
        resp.setCharacterEncoding("UTF-8");
        //往响应头里设置跨域的参数
        resp.setHeader("Access-Control-Allow-Credentials","true");
        resp.setHeader("Access-Control-Allow-Origin",req.getHeader("Origin"));
        //设置响应状态码
        resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
        try {
            //认证失败的具体消息
            resp.getWriter().print(e.getMessage());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        //认证失败
        return false;
    }

    /*
     *
     */
    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        super.doFilterInternal(request, response, chain);
    }

    //从请求中获得token
    private String getRequestToken(HttpServletRequest request){
        //请求头提取令牌
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            //从请求体中获得数据
            token = request.getParameter("token");
        }
        return token;
    }
}
