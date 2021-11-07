package com.lhalj.emos.api.service;

import com.lhalj.emos.api.db.pojo.MettingDTO;
import com.lhalj.emos.api.db.pojo.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;

public interface MeetingService {

    void insertMeeting(TbMeeting entity);

    ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);
}
