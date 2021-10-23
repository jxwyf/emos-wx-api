package com.lhalj.emos.api.config;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//同步收发消息
@Configuration
public class RabbitMQConfig {

    @Bean
    public ConnectionFactory getFactory(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("121.41.123.154");
        factory.setPort(5672);
        factory.setUsername("lwq");
        factory.setPassword("123456");
        return factory;
    }
}
