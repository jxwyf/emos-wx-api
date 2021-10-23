package com.lhalj.emos.api.service;

import com.lhalj.emos.api.db.pojo.MessageEntity;
import com.lhalj.emos.api.db.pojo.MessageRefEntity;

import java.util.HashMap;
import java.util.List;

public interface MessageService {

    String insertMessage(MessageEntity entity);

    String insertRef(MessageRefEntity entity);

    long searchUnreadCount(int userId);

    long searchLastCount(int userId);

    List<HashMap> searchMessageByPage(int userId, long strat, int length);

    HashMap searchMessageById(String id);

    long updateUnreadMessage(String id);

    long deleteMessageRefById(String id);

    long deleteUserMessageRef(int userId);
}
