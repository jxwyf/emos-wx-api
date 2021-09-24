package com.lhalj.emos.api.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.lhalj.emos.api.config.SystemConstants;
import com.lhalj.emos.api.db.dao.TbCheckinDao;
import com.lhalj.emos.api.db.dao.TbFaceModelDao;
import com.lhalj.emos.api.db.dao.TbHolidaysDao;
import com.lhalj.emos.api.db.dao.TbWorkdayDao;
import com.lhalj.emos.api.db.pojo.TbCheckin;
import com.lhalj.emos.api.exception.EmosException;
import com.lhalj.emos.api.service.CheckinService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;

/**
 * 描述:考勤业务实现
 * 邮件发送 异步执行
 */
@Service
@Slf4j
@Scope("prototype")
public class CheckinServiceImpl implements CheckinService {


    @Autowired
    private SystemConstants systemConstants;

    @Autowired
    private TbHolidaysDao holidaysDao;

    @Autowired
    private TbWorkdayDao workdayDao;

    @Autowired
    private TbCheckinDao checkinDao;

    @Autowired
    private TbFaceModelDao faceModelDao;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;



    @Override
    public String validCanCheckIn(int userId, String date) {
        boolean bool_1 = holidaysDao.searchTodayIsHolidays() !=null ? true:false;
        boolean bool_2 = workdayDao.searchTodayIsWorkday() !=null ? true:false;

        String type = "工作日";

        //判断是否是周末
        if(DateUtil.date().isWeekend()){
            type = "节假日";
        }

        if(bool_1){
            type = "节假日";
        }else if(bool_2){
            type = "工作日";
        }

        if(type.equals("节假日")){
            return "节假日不需要考勤";
        }else {
            DateTime now = DateUtil.date();
            String start = DateUtil.today() + " " + systemConstants.attendanceStartTime;
            String end = DateUtil.today() + " " + systemConstants.attendanceEndTime;
            //转换为日期对象
            DateTime attendanceStart = DateUtil.parse(start);
            DateTime attendanceEnd = DateUtil.parse(end);

            if(now.isBefore(attendanceStart)){
                return "没有到上班考勤时间";
            }else if(now.isAfter(attendanceEnd)){
                return "超过了上班考勤结束时间";
            }else {
                HashMap map = new HashMap();
                map.put("userId",userId);
                map.put("date",date);
                map.put("start",start);
                map.put("end",end);
                boolean bool = checkinDao.haveCheckin(map) !=null ? true:false;
                return bool ?  "今日已考勤,不用重复考勤":"可以考勤";
            }
        }
    }

    @Override
    public void checkin(HashMap param) {
        //判断签到
        Date d1 = DateUtil.date();//当前时间
        Date d2 = DateUtil.parse(DateUtil.today() + " " +systemConstants.attendanceTime);//上班时间
        Date d3 = DateUtil.parse(DateUtil.today() + " " +systemConstants.attendanceEndTime);//上班考勤结束时间

        int status = 1; //1正常考勤

        if(d1.compareTo(d2)<=0){
            status = 1;
        }else if(d1.compareTo(d2)>0&&d1.compareTo(d3)<0){
            status = 2;//迟到
        }

        int userId = (Integer) param.get("userId");

        //查询人脸数据
        String faceModel = faceModelDao.searchFaceModel(userId);

        if (faceModel==null) {
            throw new EmosException("不存在人脸模型");
        }
        String path = (String) param.get("path");
        //人脸对比 虚拟机问题 无法进行人脸对比 直接进入下一步
        HttpRequest request = HttpUtil.createPost(checkinUrl);
        request.form("photo", FileUtil.file(path),"targetModel",faceModel);
        HttpResponse response = request.execute();
        if (response.getStatus()!=200) {
            throw new EmosException("人脸识别服务异常");
        }
        String body = response.body();
        if ("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)) {
            throw new EmosException(body);
        }
        else if("False".equals(body)){
            throw new EmosException("签到无效,非本人签到");
        }
        else if("True".equals(body)){

        }
        //TODO 查询疫情风险等级
        //TODO 保存签到记录

    }
}
