package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ComfirmAndReturn implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {

    /**
     * 消息抵达交换机以后的回调方法
     * @param correlationData
     * @param b
     * @param s
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        if(b){
            System.out.println("消息抵达了交换机" + s);
        }else{
            System.out.println("消息未能抵达交换机,错误的信息为:" + s);
        }
    }

    /**
     * 消息未抵达队列的回调方法
     * @param message
     * @param i
     * @param s
     * @param s1
     * @param s2
     */
    @Override
    public void returnedMessage(Message message, int i, String s, String s1, String s2) {
        System.out.println("未抵达队列的消息的内容为:" + new String(message.getBody()));
        System.out.println("状态码:" + i);
        System.out.println("错误的内容为" + s);
        System.out.println("交换机" + s1);
        System.out.println("routingkey:" + s2);
    }
}
