package com.atguigu.gmall.activity.listener;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.gmall.activity.pojo.SeckillOrder;
import com.atguigu.gmall.activity.service.SeckillOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 超时未支付的秒杀订单的监听类
 */
@Component
@Log4j2
public class TimeoutSeckillOrderCancleListener {

    @Autowired
    private SeckillOrderService seckillOrderService;


    /**
     * 超时订单
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "seckill_order_timeout_queue")
    public void cancleSeckillTimeOurOrder(Channel channel, Message message){
        //获取消息
        byte[] body = message.getBody();
        String idString = new String(body);
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //消息的编号
        long deliveryTag = messageProperties.getDeliveryTag();
        try {
            SeckillOrder seckillOrder = JSONObject.parseObject(idString, SeckillOrder.class);
            //取消订单
            seckillOrderService.cancleSeckillOrder(seckillOrder.getId(),4, seckillOrder.getUserId());
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //确认消息是否被消费过
                if(messageProperties.isRedelivered()){
                    //如果已经消费过,记录到日志中去
                    log.error("取消秒杀订单失败,订单的内容为:" + idString);
                    channel.basicReject(deliveryTag, false);
                    return;
                }
                //再试一次
                channel.basicReject(deliveryTag, true);
            }catch (Exception e1){
                log.error("取消秒杀订单失败,订单的内容为:" + idString);
            }
        }

    }
}
