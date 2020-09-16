package com.atguigu.gmall.pms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class RabbitConfig {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        // 确认消息是否到达交换机，不管有没有到达交换机都会执行该方法
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack){
                System.out.println("消息已经成功的到达交换机！！！");
            } else {
                // TODO：记录到数据库
                System.out.println("消息没有到达交换机！！！-------");
            }
        });
        // 消息是否到达队列，只有消息没有到达队列时，才执行该回调
        this.rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.error("消息没有到达队列。交换机：{}，路由键：{}，消息内容：{}", exchange, routingKey, new String(message.getBody()));
            // 记录到数据库 TODO：
        });
    }
}
