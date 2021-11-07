package com.lhalj.emos.api.controller;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.lhalj.emos.api.common.utils.R;
import com.lhalj.emos.api.config.shiro.JwtUtil;
import com.lhalj.emos.api.controller.from.SearchMessageByPageForm;
import com.lhalj.emos.api.db.pojo.MettingDTO;
import com.lhalj.emos.api.service.MeetingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/meeting")
@Api("会议模块网络接口")
public class MeetingController {


    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MeetingService service;


    @PostMapping("/searchMyMeetingListByPage")
    @ApiOperation("查询会议列表分页数据")
    public R searchMyMeetingListByPage(@Valid @RequestBody SearchMessageByPageForm form, @RequestHeader("token")String token){
        int userId = jwtUtil.getUserId(token);
        int page = form.getPage();
        int length = form.getLength();
        long start = (page - 1) * length;
        HashMap map = new HashMap();
        map.put("userId",userId);
        map.put("start",start);
        map.put("length",length);
        ArrayList<HashMap> list = service.searchMyMeetingListByPage(map);
        System.out.println(list);
        return R.ok().put("result",list);
    }


    @GetMapping("/searchMyMeetingListByPage1")
    @ApiOperation("查询会议列表分页数据")
    public R searchMyMeetingListByPage(){
//        int userId = jwtUtil.getUserId(token);
        int page = 1;
        int length = 5;
        long start = (page - 1) * length;
        HashMap map = new HashMap();
        map.put("userId",4);
        map.put("start",start);
        map.put("length",length);
        //TODO
        ArrayList<HashMap> list = service.searchMyMeetingListByPage(map);
        R result = R.ok().put("result", list);
        System.out.println(result);
        return result;
    }



}
