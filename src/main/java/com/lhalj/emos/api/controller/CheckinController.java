package com.lhalj.emos.api.controller;

import cn.hutool.core.date.DateUtil;
import com.lhalj.emos.api.common.utils.R;
import com.lhalj.emos.api.config.shiro.JwtUtil;
import com.lhalj.emos.api.service.CheckinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 描述:签到
 */
@RequestMapping("/checkin")
@RestController
@Api("签到模块web接口")
@Slf4j
public class CheckinController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CheckinService checkinService;

    @GetMapping("/validCanCheckIn")
    @ApiOperation("查看用户今天是否可以签到")
    public R validCanCheckIn(@RequestHeader("tokne") String token){
        int userId = jwtUtil.getUserId(token);
        String result = checkinService.validCanCheckIn(userId, DateUtil.today());
        return R.ok(result);
    }

}
