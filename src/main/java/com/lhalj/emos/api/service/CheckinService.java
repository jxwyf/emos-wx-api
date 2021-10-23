package com.lhalj.emos.api.service;

import java.util.ArrayList;
import java.util.HashMap;

public interface CheckinService {

    String validCanCheckIn(int userId,String date);

    //保存签到记录
    void checkin(HashMap param);

    //保存人脸数据
    void createFaceModel(int userId,String path);


    HashMap searchTodayCheckin(int userId);

    long searchCheckinDays(int userId);

    ArrayList<HashMap> searchWeekCheckin(HashMap param);

    //查询月考勤
    ArrayList<HashMap> searchMonthCheckin(HashMap param);

}
