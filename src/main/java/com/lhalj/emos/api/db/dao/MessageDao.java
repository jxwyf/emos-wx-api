package com.lhalj.emos.api.db.dao;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.unit.DataUnit;
import cn.hutool.json.JSONObject;
import com.lhalj.emos.api.db.pojo.MessageEntity;

import com.lhalj.emos.api.db.pojo.MessageRefEntity;
import lombok.var;
import org.jsoup.helper.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public class MessageDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    //保存信息
    public String insert(MessageEntity entity){
        //北京时间转换为格林尼治时间
        Date sendTime = entity.getSendTime();
        sendTime = DateUtil.offset(sendTime, DateField.HOUR, 8);
        entity.setSendTime(sendTime);
        entity = mongoTemplate.save(entity);
        return entity.get_id();
    }

    //分页查询消息
    public List<HashMap> searchMessageByPage(int userId,long strat,int length){
        JSONObject json = new JSONObject();
        json.set("$toString","$_id");

        //集合连接
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.addFields().addField("id").withValue(json).build(),
                Aggregation.lookup("message_ref","id","messageId","ref"),
                Aggregation.match(Criteria.where("ref.receiverId").is(userId)),
                Aggregation.sort(Sort.Direction.DESC,"sendTime"),
                Aggregation.skip(strat),
                Aggregation.limit(length)
        );

        //联合查询
        AggregationResults<HashMap> results = mongoTemplate.aggregate(aggregation, "message", HashMap.class);

        List<HashMap> list = results.getMappedResults();

        //处理消息
        list.forEach(one->{
            List<MessageRefEntity> refList = (List<MessageRefEntity>) one.get("ref");
            MessageRefEntity entity = refList.get(0);
            Boolean readFlag = entity.getReadFlag();
            String refId = entity.get_id();

            one.put("readFlag",readFlag);
            one.put("refId",refId);
            one.remove("ref");
            one.remove("_id");

            //时间转化
            Date sendTime = (Date) one.get("sendTime");
            sendTime = DateUtil.offset(sendTime,DateField.HOUR,-8);

            //判断是否当天发送的信息
            String today = DateUtil.today();
            if(today.equals(DateUtil.date(sendTime))){
                one.put("sendTime",DateUtil.format(sendTime,"HH:mm"));
            }else {
                one.put("sendTime",DateUtil.format(sendTime,"yyyy/MM/dd"));
            }

        });

        return list;
    }

    //根据id查询消息
    public HashMap searchMessageById(String id){
       HashMap map =  mongoTemplate.findById(id,HashMap.class,"message");
        //时间转化
        Date sendTime = (Date) map.get("sendTime");
        sendTime = DateUtil.offset(sendTime,DateField.HOUR,-8);
        map.replace("sendTime",DateUtil.format(sendTime,"yyyy-MM-dd HH:mm"));
        return map;
    }

}
