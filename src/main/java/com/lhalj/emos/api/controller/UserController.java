package com.lhalj.emos.api.controller;

import cn.hutool.json.JSONUtil;
import com.lhalj.emos.api.common.utils.R;
import com.lhalj.emos.api.config.shiro.JwtUtil;
import com.lhalj.emos.api.controller.from.LoginForm;
import com.lhalj.emos.api.controller.from.RegisterForm;
import com.lhalj.emos.api.controller.from.SearchMembersForm;
import com.lhalj.emos.api.controller.from.SearchUserGroupByDeptForm;
import com.lhalj.emos.api.exception.EmosException;
import com.lhalj.emos.api.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    //登录
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public R login(@Valid @RequestBody LoginForm form){
        int id = userService.login(form.getCode());
        String token = jwtUtil.createToken(id);
        //查询权限
        Set<String> permsSet = userService.searchUserPermissions(id);
        saveChcheToken(token,id);

        return R.ok("登录成功").put("token",token).put("permission",permsSet);
    }

    @GetMapping("/searchUserSummary")
    @ApiOperation("查询用户信息")
    public R searchUserSummary(@RequestHeader("token")String token){
        int userId = jwtUtil.getUserId(token);
        HashMap map = userService.searchUserSummary(userId);
        return R.ok().put("result",map);
    }

    @PostMapping("/searchUserGroupByDept")
    @ApiOperation("查询员工列表,按照部门分组排列")
    @RequiresPermissions(value = {"ROOT","EMPLOYEE:SELECT"},logical = Logical.OR)
    public R searchUserGroupBydept(@Valid @RequestBody SearchUserGroupByDeptForm form){
        ArrayList<HashMap> list = userService.searchUserGroupByDept(form.getKeyword());
        return R.ok().put("result",list);
    }

    @PostMapping("/searchMembers")
    @ApiOperation("查询成员")
    @RequiresPermissions(value = {"ROOT","EMPLOYEE:SELECT"},logical = Logical.OR)
    public R searchMembers(@Valid @RequestBody SearchMembersForm form){
        //判断传递进来的参数为json数组
        if (!JSONUtil.isJsonArray(form.getMembers())) {
            throw new EmosException("members不是json数组");
        }
        List<Integer> param = JSONUtil.parseArray(form.getMembers()).toList(Integer.class);
        ArrayList<HashMap> list = userService.searchMembers(param);
        return R.ok().put("result",list);
    }


    //保存redis
    private void saveChcheToken(String token, int userId){
        redisTemplate.opsForValue().set(token,userId + "",cacheExpire);
    }
}
