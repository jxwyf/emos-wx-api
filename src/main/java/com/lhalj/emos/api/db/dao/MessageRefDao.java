package com.lhalj.emos.api.db.dao;

import com.lhalj.emos.api.db.pojo.MessageRefEntity;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRefDao {


    @Autowired
    private MongoTemplate mongoTemplate;


    //保存
    public String insert(MessageRefEntity entity){
        entity= mongoTemplate.save(entity);
        return entity.get_id();
    }

    //查询未读消息的数量
    public long searchUnreadCount(int userId){
        Query query = new Query();
        //查询条件
        query.addCriteria(Criteria.where("readFlag").is(false).and("receiverId").is(userId));
        long count = mongoTemplate.count(query, MessageRefEntity.class);
        return count;
    }

    //查询最新消息
    public long searchLastCount(int userId){
        Query query = new Query();
        query.addCriteria(Criteria.where("lastFlag").is(true).and("receiverId").is(userId));
        Update update = new Update();
        update.set("lastFlag",false); //表示不是新消息
        UpdateResult result = mongoTemplate.updateMulti(query, update, "message_ref");
        long rows = result.getModifiedCount();//获取修改了多少条
        return rows;
    }

    //修改消息状态 未读改为已读
    public long updateUnreadMessage(String id){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        Update update = new Update();
        update.set("readFlag",true); //表示已读
        UpdateResult result = mongoTemplate.updateFirst(query, update, "message_ref");
        long rows = result.getModifiedCount();//获取修改了多少条
        return rows;
    }

    // 根据主键值删除消息记录
    public long deleteMessageRefById(String id){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        DeleteResult result = mongoTemplate.remove(query, "message_ref");
        long rows = result.getDeletedCount();
        return rows;
    }

    //根据用户id删除用户所有消息
    public long deleteUserMessageRef(int userId){
        Query query = new Query();
        query.addCriteria(Criteria.where("receiverId").is(userId));
        DeleteResult result = mongoTemplate.remove(query, "message_ref");
        long rows = result.getDeletedCount();
        return rows;

    }
}
