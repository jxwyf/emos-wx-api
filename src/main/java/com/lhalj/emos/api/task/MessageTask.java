package com.lhalj.emos.api.task;

import com.lhalj.emos.api.db.dao.MessageRefDao;
import com.lhalj.emos.api.db.pojo.MessageEntity;
import com.lhalj.emos.api.db.pojo.MessageRefEntity;
import com.lhalj.emos.api.exception.EmosException;
import com.lhalj.emos.api.service.MessageService;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class MessageTask {

    @Autowired
    private ConnectionFactory factory;

    @Autowired
    private MessageService messageService;

    public void send(String topic, MessageEntity entity){
        String id = messageService.insertMessage(entity);
        //往RabbitMQ发送消息
        try(Connection connection = factory.newConnection();//创建连接
            //通道
            Channel channel = connection.createChannel();
        ){
            channel.queueDeclare(topic,true,false,false,null);
            HashMap map = new HashMap();
            map.put("messageId",id);
            AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().headers(map).build();
            //发出消息
            channel.basicPublish("",topic,properties,entity.getMsg().getBytes());
            log.debug("消息发送成功");

        }catch (Exception e){
            log.error("执行异常",e);
            throw new EmosException("向MQ发送消息失败");
        }
    }

    @Async
    public void sendAsync(String topic, MessageEntity entity){
        this.send(topic, entity);
    }

    public int receive(String topic){
        int i = 0;
        try(Connection connection = factory.newConnection();//创建连接
            //通道
            Channel channel = connection.createChannel();
        ){
            channel.queueDeclare(topic,true,false,false,null);
            //接收数据
            while (true){
                GetResponse response = channel.basicGet(topic, false);
                //响应不为空
                if(response!=null){
                    AMQP.BasicProperties properties = response.getProps();
                    //获取请求头数据
                    Map<String, Object> map = properties.getHeaders();
                    String messageId = map.get("messageId").toString();
                    //获取消息正文
                    byte[] body = response.getBody();
                    String message = new String(body);
                    log.debug("从RabbitMQ接收到的消息:"+message);

                    MessageRefEntity entity = new MessageRefEntity();
                    entity.setMessageId(messageId);
                    entity.setReceiverId(Integer.parseInt(topic));
                    entity.setReadFlag(false); //未读
                    entity.setLastFlag(true);
                    messageService.insertRef(entity);

                    long deliveryTag = response.getEnvelope().getDeliveryTag();
                    //返回ACK应答
                    channel.basicAck(deliveryTag,false);
                    i++;
                }else {
                    break;
                }
            }
        }catch (Exception e){
            log.error("执行异常",e);
            throw new EmosException("接收消息失败");
        }

        return i;
    }

    @Async
    public int receiveAsync(String topic){
        return receive(topic);
    }


    public void deleteQueue(String topic){
        try(Connection connection = factory.newConnection();//创建连接
            //通道
            Channel channel = connection.createChannel();
        ){
            channel.queueDelete(topic);
            log.debug("消息队列成功删除");


        }catch (Exception e){
            log.error("执行异常",e);
            throw new EmosException("删除消息失败");
        }
    }

    @Async
    public void deleteQueueAsync(String topic){
        deleteQueue(topic);
    }
}
