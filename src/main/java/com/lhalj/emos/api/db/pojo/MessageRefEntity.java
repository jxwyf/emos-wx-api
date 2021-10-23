package com.lhalj.emos.api.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "message_ref")
public class MessageRefEntity implements Serializable {

    @Id
    private String _id;

    @Indexed
    private String messageId;

    //接收人ID
    @Indexed
    private Integer receiverId;

    //是否已读
    @Indexed
    private Boolean readFlag;

    // 是否为新接收的消息
    @Indexed
    private Boolean lastFlag;
}
