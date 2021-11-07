package com.lhalj.emos.api.service.impl;

import cn.hutool.json.JSONArray;
import com.lhalj.emos.api.db.dao.TbMeetingDao;
import com.lhalj.emos.api.db.pojo.TbMeeting;
import com.lhalj.emos.api.exception.EmosException;
import com.lhalj.emos.api.service.MeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
@Slf4j
public class MeetingServiceImpl implements MeetingService {


    @Autowired
    private TbMeetingDao meetingDao;

    @Override
    public void insertMeeting(TbMeeting entity) {
        int row = meetingDao.insertMeeting(entity);
        if(row!=1){
            throw new EmosException("会议添加失败");
        }
        //TODO 开启审批工作流
    }

    @Override
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param) {

        ArrayList<HashMap> list = meetingDao.searchMyMeetingListByPage(param);

        String date = null;

        ArrayList resultList = new ArrayList();

        HashMap resultMap = null;
        JSONArray array = null;

        //按日期分组
        for(HashMap map:list){
            String temp = map.get("date").toString();
            if(!temp.equals(date)){
                date = temp;
                resultMap = new HashMap();
                resultMap.put("date",date);
                array = new JSONArray();
                resultMap.put("list",array);
                resultList.add(resultMap);
            }
            array.put(map);
        }


        return resultList;
    }
}
