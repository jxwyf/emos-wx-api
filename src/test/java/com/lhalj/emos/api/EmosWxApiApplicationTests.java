package com.lhalj.emos.api;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.lhalj.emos.api.db.pojo.MessageEntity;
import com.lhalj.emos.api.db.pojo.MessageRefEntity;
import com.lhalj.emos.api.db.pojo.TbMeeting;
import com.lhalj.emos.api.service.MeetingService;
import com.lhalj.emos.api.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class EmosWxApiApplicationTests {


    @Autowired
    private MessageService messageService;

    @Autowired
    private MeetingService service;


    @Test
    void contextLoads() {
        for (int i = 0; i < 100 ; i++) {
            MessageEntity entity = new MessageEntity();
            entity.setUuid(IdUtil.simpleUUID());
            entity.setSenderId(0);
            entity.setSenderName("系统消息");
            entity.setMsg("这是第"+ i +"条消息");
            entity.setSendTime(new Date());
            String id = messageService.insertMessage(entity);

            MessageRefEntity ref = new MessageRefEntity();
            ref.setMessageId(id);
            ref.setReceiverId(4);//接收人id
            ref.setLastFlag(true);
            ref.setReadFlag(false);
            messageService.insertRef(ref);
        }
    }

    @Test
    void createMeetingData() {
        for (int i = 201; i < 300 ; i++) {
            TbMeeting meeting = new TbMeeting();
            meeting.setId((long)i);
            meeting.setUuid(IdUtil.simpleUUID());
            meeting.setTitle("测试会议" + i);
            meeting.setCreatorId(4L); //root用户ID
            meeting.setDate(DateUtil.today());
            meeting.setPlace("线上会议室");
            meeting.setStart("08:30");
            meeting.setEnd("10:30");
            meeting.setType((short) 1);
            meeting.setMembers("[4,16]");
            meeting.setDesc("会议研讨Emos项目上线测试");
            meeting.setInstanceId(IdUtil.simpleUUID());
            meeting.setStatus((short) 3);
            service.insertMeeting(meeting);
        }
    }

}
