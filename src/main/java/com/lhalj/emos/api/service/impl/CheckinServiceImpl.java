package com.lhalj.emos.api.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.lhalj.emos.api.config.SystemConstants;
import com.lhalj.emos.api.db.dao.*;
import com.lhalj.emos.api.db.pojo.TbCheckin;
import com.lhalj.emos.api.db.pojo.TbFaceModel;
import com.lhalj.emos.api.exception.EmosException;
import com.lhalj.emos.api.service.CheckinService;
import com.lhalj.emos.api.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    @Autowired
    private TbCityDao cityDao;

    @Value("${emos.email.hr}")
    private String hrEmail;

    @Autowired
    private EmailTask emailTask;

    @Autowired
    private TbUserDao userDao;



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
        if (response.getStatus()==200) {
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
        // 查询疫情风险等级
        int risk = 1;//低风险
        //查询城市简称
        String city = (String) param.get("city");
        String district = (String) param.get("district");

        String address = (String) param.get("address");
        String country = (String) param.get("country");
        String province = (String) param.get("province");

        if(!StrUtil.isBlank(city)&&!StrUtil.isBlank(district)){
            String code = cityDao.searchCode(city);
            //查询地区风险
            try{
                String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" + district;
                Document document = Jsoup.connect(url).get();
                //解析html
                Elements elements = document.getElementsByClass("list-content");
                if(elements.size()>0){
                    Element element = elements.get(0);
                    String result = element.select("p:last-child").text();
                    if ("高风险".equals(result)) {
                        risk = 3;
                        // 发送告警邮件
                        HashMap<String, String> map = userDao.searchNameAndDept(userId);
                        String name = map.get("name");
                        String deptNmae = map.get("dept_name");
                        deptNmae = deptNmae !=null ?deptNmae : " ";
                        //邮件封装对象
                        SimpleMailMessage message = new SimpleMailMessage();
                        message.setTo(hrEmail);
                        message.setSubject("员工" + name +"身处高风险疫情地区警告");
                        message.setText(deptNmae + "员工" + name + "," + DateUtil.format(new Date(),"yyyy年MM月dd日") + "处于" + address +",属于疫情高风险地区 请及时和员工联系 核实情况");
                        emailTask.sendAsync(message);
                    }else if("中风险".equals(result)){
                        risk = 2;
                    }

                }
            }catch (Exception e){
                throw new EmosException("获取风险等级失败");
            }

        }
        //保存签到记录

        // 发送告警邮件
        HashMap<String, String> map = userDao.searchNameAndDept(userId);
        //邮件封装对象
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(hrEmail);
        message.setSubject("员工" + "刘温情" +"身处高风险疫情地区警告");
        message.setText("开发部" + "员工" + "刘温情" + "," + DateUtil.format(new Date(),"yyyy年MM月dd日") + "处于" + address +",属于疫情高风险地区 请及时和员工联系 核实情况");
        emailTask.sendAsync(message);

        TbCheckin entity = new TbCheckin();
        entity.setUserId(userId);
        entity.setRisk(risk);
        entity.setAddress(address);
        entity.setCountry(country);
        entity.setCity(city);
        entity.setProvince(province);
        entity.setDistrict(district);
        entity.setStatus((byte) status);
        entity.setDate(DateUtil.today());
        entity.setCreateTime(d1);

        checkinDao.insert(entity);

    }

    @Override
    public void createFaceModel(int userId, String path) {
        HttpRequest request = HttpUtil.createPost(createFaceModelUrl);
        request.form("photo",FileUtil.file(path));

        HttpResponse response = request.execute();
        String body = response.body();

        if("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)){
            throw new EmosException(body);
        }
        TbFaceModel entity = new TbFaceModel();
        entity.setUserId(userId);
        entity.setFaceModel(userId+"");
        faceModelDao.insert(entity);

    }

    @Override
    public HashMap searchTodayCheckin(int userId) {
        HashMap map = checkinDao.searchTodayCheckin(userId);
        return map;
    }

    @Override
    public long searchCheckinDays(int userId) {
        long days = checkinDao.searchCheckinDays(userId);
        return days;
    }

    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        ArrayList<HashMap> checkinList = checkinDao.searchWeekCheckin(param);
        //查询本周特殊节假日
        ArrayList<String> holidaysList = holidaysDao.searchHolidaysInRange(param);
        //查询本周特殊工作日
        ArrayList<String> workdayList = workdayDao.searchWorkdayInRange(param);
        //本周起始日期
        DateTime startDate = DateUtil.parseDate(param.get("startDate").toString());
        DateTime endDate = DateUtil.parseDate(param.get("endDate").toString());
        //七天日期对象
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        ArrayList<HashMap> list = new ArrayList<>();
        range.forEach(one->{
            String date = one.toString("yyyy-MM-dd");
            String type = "工作日";
            if(one.isWeekend()){
                type = "节假日";
            }
            if(holidaysList!=null&&holidaysList.contains(date)){
                type = "节假日";
            }else if(workdayList!=null&&workdayList.contains(date)){
                type = "工作日";
            }
            String status = "";
            //one 本周某一天
            if(type.equals("工作日")&&DateUtil.compare(one,one)<=0){
                status = "缺勤";
                //是否考勤
                boolean flag = false;
                //判定本周考勤情况
                for (HashMap<String,String> map:checkinList){
                    if (map.containsValue(date)) {
                        status = map.get("status");
                        flag = true;
                        break;
                    }
                }
                //当天考勤结束时间
                DateTime endTime = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceEndTime);
                System.out.println();
                String today = DateUtil.today();
                //
                if(date.equals(today)&&DateUtil.date().isBefore(endTime)&&flag==false){
                    status="";
                }
            }
            HashMap map = new HashMap();
            map.put("date",date);
            map.put("status",status);
            map.put("type",type);
            map.put("type",type);
            map.put("day",one.dayOfWeekEnum().toChinese("周"));
            list.add(map);
        });

        return list;
    }

    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap param) {
        return this.searchWeekCheckin(param);
    }
}
