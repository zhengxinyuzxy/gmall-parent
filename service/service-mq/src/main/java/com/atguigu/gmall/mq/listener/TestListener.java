package com.atguigu.gmall.mq.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消息的消费者
 */
@Component
@Log4j2
public class TestListener {


    /**
     * 消费者1
     */
    @RabbitListener(queues = "queue_java0422_1")
    public void listener1(Channel channel,Message message){
        byte[] body = message.getBody();
        String s = new String(body);
        MessageProperties messageProperties = message.getMessageProperties();
        long deliveryTag = messageProperties.getDeliveryTag();
        System.out.println(deliveryTag);
        System.out.println(s);
        try {
            int i = 1/0;
            channel.basicAck(deliveryTag,false);

        }catch (Exception e){
            e.printStackTrace();
        }try {
            if (messageProperties.isRedelivered()){
                channel.basicReject(deliveryTag,false);
                log.error(s);
                return;
            }
            channel.basicReject(deliveryTag,true);
        }catch (Exception e1){
            System.out.println("拒绝");
        }
    }
}

