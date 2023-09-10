package com.atguigu.gmall.activity.listener;

import com.atguigu.gmall.activity.service.SeckillOrderService;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 秒杀订单支付的监听类
 */
@Component
@Log4j2
public class SeckillOrderPayListener {

    @Autowired
    private SeckillOrderService seckillOrderService;


    @RabbitListener(queues = "秒杀支付队列")
    public void seckillOrderPayMessage(Channel channel, Message message){
        //获取消息
        byte[] body = message.getBody();
        String msg = new String(body);
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //消息的编号
        long deliveryTag = messageProperties.getDeliveryTag();
        try {
            //修改秒杀订单的支付状态
            seckillOrderService.updatePayStatus(msg);
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //确认消息是否被消费过
                if(messageProperties.isRedelivered()){
                    //如果已经消费过,记录到日志中去
                    log.error("秒杀订单支付结果处理失败,内容为:" + msg);
                    channel.basicReject(deliveryTag, false);
                    return;
                }
                //再试一次
                channel.basicReject(deliveryTag, true);
            }catch (Exception e1){
                log.error("秒杀订单支付结果处理失败,内容为:" + msg);
            }
        }
    }
}
