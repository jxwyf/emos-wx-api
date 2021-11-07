package com.lhalj.emos.api.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "message")
public class MessageEntity implements Serializable {

    //主键
    @Id
    private String _id;

    @Indexed(unique = true) //唯一性约束
    private String uuid;

    //发送者id
    @Indexed
    private Integer senderId;

    private String senderPhoto = "https://static-1258386385.cos.ap-beijing.myqcloud.com/img/System.jpg";;

    private String senderName;

    private String msg;

    @Indexed
    private Date sendTime;
}
