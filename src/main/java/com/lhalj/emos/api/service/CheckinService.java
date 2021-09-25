package com.lhalj.emos.api.service;

import java.util.HashMap;

public interface CheckinService {

    String validCanCheckIn(int userId,String date);

    //保存签到记录
    void checkin(HashMap param);

    //保存人脸数据
    void createFaceModel(int userId,String path);
}
