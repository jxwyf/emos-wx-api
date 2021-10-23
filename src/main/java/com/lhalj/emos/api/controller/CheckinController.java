package com.lhalj.emos.api.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.lhalj.emos.api.common.utils.R;
import com.lhalj.emos.api.config.SystemConstants;
import com.lhalj.emos.api.config.shiro.JwtUtil;
import com.lhalj.emos.api.controller.from.CheckinForm;
import com.lhalj.emos.api.controller.from.SearchMonthCheckinForm;
import com.lhalj.emos.api.exception.EmosException;
import com.lhalj.emos.api.service.CheckinService;
import com.lhalj.emos.api.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.awt.font.MultipleMaster;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

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

    @Value("${emos.image-folder}")
    private String imageFolder;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConstants constants;

    @GetMapping("/validCanCheckIn")
    @ApiOperation("查看用户今天是否可以签到")
    public R validCanCheckIn(@RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        String result = checkinService.validCanCheckIn(userId, DateUtil.today());
        return R.ok(result);
    }

    @PostMapping("/checkin")
    @ApiOperation("签到")
    public R checkin(@Valid CheckinForm form,
                     @RequestParam("photo") MultipartFile file,
                     @RequestHeader("token") String token){

        if(file==null){
            return R.error("没有上传文件");
        }

        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();

        if (!fileName.endsWith(".jpg")) {
            return R.error("必须提交JPG格式图片");
        }else {
            String path = imageFolder + "/" +fileName;
            try{
                //保存图片
                file.transferTo(Paths.get(path));
                HashMap param=new HashMap();
                param.put("userId",userId);
                param.put("path",path);
                param.put("city",form.getCity());
                param.put("district",form.getDistrict());
                param.put("address",form.getAddress());
                param.put("country",form.getCountry());
                param.put("province",form.getProvince());
                checkinService.checkin(param);
                return R.ok("签到成功");
            }catch (IOException e){
                throw new EmosException("图片保存错误");
            }finally {
                //删除图片
                FileUtil.del(path);
            }
        }
    }

    @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestParam("photo") MultipartFile file,@RequestHeader("token")String token){
        int userId = jwtUtil.getUserId(token);

        if (file==null) {
            return R.error("没有上传文件");
        }
        String fileName = file.getOriginalFilename().toLowerCase();
        String path = imageFolder + "/" +fileName;
        if (!fileName.endsWith(".jpg")) {
            return R.error("必须提交JPG格式图片");
        }else {
            try {
                file.transferTo(Paths.get(path));
                checkinService.createFaceModel(userId,path);
                return R.ok("人脸建模成功");
            }catch (IOException e){

            }
            return R.ok("人脸建模成功");
        }
    }

    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询用户当日签到数据")
    private HashMap searchTodayCheckin(@RequestHeader("token")String token){
        int userId = jwtUtil.getUserId(token);

        HashMap map = checkinService.searchTodayCheckin(userId);
        //考勤开始时间
        map.put("attendanceTime",constants.attendanceTime);
        map.put("closingTime",constants.closingTime);
        //查询员工签到总天数
        long days = checkinService.searchCheckinDays(userId);
        map.put("checkinDays",days);

        //判断日期是否在用户入职之前
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());
        if(startDate.isBefore(hiredate)){
            startDate = hiredate;
        }

        DateTime endDate = DateUtil.endOfWeek(DateUtil.date());
        HashMap param = new HashMap();
        param.put("startDate",startDate.toString());
        param.put("endDate",endDate.toString());
        param.put("userId",userId);

        ArrayList<HashMap> list = checkinService.searchWeekCheckin(param);
        map.put("weekCheckin",list);
        return R.ok().put("result",map);
    }


    @PostMapping("/searchMonthCheckin")
    @ApiOperation("查询用户某月签到数据")
    public R searchMonthCheckin(@Valid @RequestBody SearchMonthCheckinForm form,@RequestHeader("token")String token){
        int userId = jwtUtil.getUserId(token);
        //查询入职日期
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        //年份转换
        String month = form.getMonth() < 10 ? "0" + form.getMonth() : form.getMonth().toString();
        //输入的月份
        DateTime startDate = DateUtil.parse(form.getYear() + "-" +month + "-01");

        //入职时间当月第一天
        if(startDate.isBefore(DateUtil.beginOfMonth(hiredate))){
            throw new EmosException("只能查询考勤之后日期的数据");
        }
        if(startDate.isBefore(hiredate)){
            startDate = hiredate;
        }
        //获取当月最后一天
        DateTime endDate = DateUtil.endOfMonth(startDate);
        //当前时间
        DateTime nowDate = DateUtil.date();

        if(endDate.isAfter(nowDate)) {
            endDate = nowDate;
            if(startDate.after(endDate)){
                throw new EmosException("只能查询考勤之后日期的数据");
            }
        }


        HashMap param = new HashMap();
        param.put("userId",userId);
        param.put("startDate",startDate.toString());
        param.put("endDate",endDate.toString());
        ArrayList<HashMap> list = checkinService.searchMonthCheckin(param);
        int sum_1 = 0;
        int sum_2 = 0;
        int sum_3 = 0;
        for (HashMap<String,String> one:list){
            String type = one.get("type");
            String status = one.get("status");
            if("工作日".equals(type)){
                if("正常".equals(status)){
                    sum_1++;
                }else if("迟到".equals(status)){
                    sum_2++;
                }else if("缺勤".equals(status)){
                    sum_3++;
                }
            }
        }

        return R.ok().put("list",list).put("sum_1",sum_1).put("sum_2",sum_2).put("sum_3",sum_3);
    }



}
