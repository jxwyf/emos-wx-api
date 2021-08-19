package com.lhalj.emos.api.controller;

import com.lhalj.emos.api.common.utils.R;
import com.lhalj.emos.api.controller.from.TestSayHelloForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 描述:
 */
@RestController
@RequestMapping("/test")
@Api("测试web接口")
public class TestController {

    @PostMapping("/sayhello")
    @ApiOperation("最简单的测试方法")
    public R sayHello(@Valid @RequestBody TestSayHelloForm form){
        return R.ok().put("message", "Hello, " + form.getName());
    }
}
