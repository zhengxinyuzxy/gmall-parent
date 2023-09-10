package com.atguigu.gmall.rabbit.config;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

/**
 * 消息发送服务类
 */
@Service
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送普通消息
     * @param exchange
     * @param routingKey
     * @param message
     * @return
     */
    public Boolean sendMessage(String exchange, String routingKey, Object message){
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        return true;
    }

    /**
     * 发送带有效期的消息
     * @param exchange
     * @param routingKey
     * @param message
     * @param time
     * @return
     */
    public Boolean sendMessage(String exchange, String routingKey, Object message, @NotNull String time){
        //发送带有效期的消息
        rabbitTemplate.convertAndSend(exchange, routingKey, message, new MessagePostProcessor() {
            /**
             * 设置消息的有效期
             * @param message
             * @return
             * @throws AmqpException
             */
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //获取消息的属性
                MessageProperties messageProperties = message.getMessageProperties();
                //设置消息的过期时间:字符串类型的参数----单位为毫秒
                messageProperties.setExpiration(time);
                //返回消息对象
                return message;
            }
        });
        return true;
    }
}