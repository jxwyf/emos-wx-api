package com.lhalj.emos.api.config.shiro;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.log4j.Log4j2;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 描述:令牌工具类
 */
@Component
@Slf4j
public class JwtUtil {

    // jwt密钥
    @Value("${emos.jwt.secret}")
    private String secret;

    // 过期时间
    @Value("${emos.jwt.expire}")
    private int expire;

    //生成TOKEN
    public  String createToken(int userId){
        //获取5天后的日期
        Date date = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, 5);
        //生成密钥 加密算法 对密钥进行加密 把密钥封装成加密算法
        Algorithm algorithm = Algorithm.HMAC256(secret);
        //创建内部类对象
        JWTCreator.Builder builder = JWT.create();
        String token =  builder.withClaim("userId",userId) //用户id
                .withExpiresAt(date) // 过期时间
                .sign(algorithm); //加密算法
        return token;
    }

    //根据用户Token获得用户ID
    public int getUserId(String token){
        //创建解码对象
        DecodedJWT jwt = JWT.decode(token);
        //获得用户Id
        int userId = jwt.getClaim("userId").asInt();
        return userId;
    }

    //验证用户令牌字符串有效性
    public void verifierToken(String token){
        //生成密钥 加密算法 对密钥进行加密 把密钥封装成加密算法
        Algorithm algorithm = Algorithm.HMAC256(secret);
        //验证对象
        JWTVerifier verifier = JWT.require(algorithm).build();
        //验证方法
        verifier.verify(token);
    }


}
