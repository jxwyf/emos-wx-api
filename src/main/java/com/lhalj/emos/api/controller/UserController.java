package com.lhalj.emos.api.controller;

import com.lhalj.emos.api.common.utils.R;
import com.lhalj.emos.api.config.shiro.JwtUtil;
import com.lhalj.emos.api.controller.from.RegisterForm;
import com.lhalj.emos.api.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

/**
 * 描述:用户
 */
@RestController
@RequestMapping("/user")
@Api("用户模块web接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;


    @PostMapping("/register")
    @ApiOperation("注册用户")
    public R register(@Valid @RequestBody RegisterForm form){
        int id = userService.registerUser(form.getRegisterCode(), form.getCode(), form.getNickname(), form.getPhoto());

        String token = jwtUtil.createToken(id);

        Set<String> permsSet = userService.searchUserPermissions(id);
        //缓存令牌
        saveChcheToken(token,id);

        return R.ok("用户注册成功").put("token",token).put("permission",permsSet);
    }


    //保存redis
    private void saveChcheToken(String token, int userId){
        redisTemplate.opsForValue().set(token,userId + "",cacheExpire);
    }
}
