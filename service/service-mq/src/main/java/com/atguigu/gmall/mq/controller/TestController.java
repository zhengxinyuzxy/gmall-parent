package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.mq.config.ComfirmAndReturn;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/mq")
public class TestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ComfirmAndReturn comfirmAndReturn;
    /**
     * 发送消息测试
     * @return
     */
    @GetMapping(value = "/send")
    public String send(){
        rabbitTemplate.setReturnCallback(comfirmAndReturn);
        rabbitTemplate.setConfirmCallback(comfirmAndReturn);
        rabbitTemplate.convertAndSend("exchange_java0422", "java.abc", "java的第一条消息");
        return "success";
    }

    /**
     * 发送消息测试
     * @return
     */
    @GetMapping(value = "/test")
    public String test(){
        System.out.println("发送延迟消息的时间为:" + System.currentTimeMillis());
        rabbitTemplate.convertAndSend("nomal_exchange",
                "dead.abc",
                "延迟消息",
                new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                MessageProperties messageProperties = message.getMessageProperties();
                messageProperties.setExpiration("10000");
                return message;
            }
        });
        return "success";
    }
}
