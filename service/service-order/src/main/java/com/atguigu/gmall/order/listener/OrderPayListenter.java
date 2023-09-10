package com.atguigu.gmall.order.listener;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 订单支付的监听类
 */
@Component
@Log4j2
public class OrderPayListenter {

    @Autowired
    private OrderService orderService;

    /**
     * 监听微信支付的支付结果
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "wx_pay_queue")
    public void orderPayWx(Channel channel, Message message){
        //获取消息
        byte[] body = message.getBody();
        String mapString = new String(body);
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //消息的编号
        long deliveryTag = messageProperties.getDeliveryTag();
        try {
            //反序列化
            Map<String,String> map = JSONObject.parseObject(mapString, Map.class);
            //修改订单
            orderService.updateOrder(map, 1);
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //确认消息是否被消费过
                if(messageProperties.isRedelivered()){
                    //如果已经消费过,记录到日志中去
                    log.error("修改订单失败,订单的信息为:" + mapString);
                    channel.basicReject(deliveryTag, false);
                    return;
                }
                //再试一次
                channel.basicReject(deliveryTag, true);
            }catch (Exception e1){
                log.error("修改订单失败,订单的信息为:" + mapString);
            }
        }
    }

    /**
     * 监听支付宝支付的支付结果
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "ali_pay_queue")
    public void orderPayAli(Channel channel, Message message){
        //获取消息
        byte[] body = message.getBody();
        String mapString = new String(body);
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //消息的编号
        long deliveryTag = messageProperties.getDeliveryTag();
        try {
            //反序列化
            Map<String,String> map = JSONObject.parseObject(mapString, Map.class);
            //修改订单
            orderService.updateOrder(map, 0);
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //确认消息是否被消费过
                if(messageProperties.isRedelivered()){
                    //如果已经消费过,记录到日志中去
                    log.error("修改订单失败,订单的信息为:" + mapString);
                    channel.basicReject(deliveryTag, false);
                    return;
                }
                //再试一次
                channel.basicReject(deliveryTag, true);
            }catch (Exception e1){
                log.error("修改订单失败,订单的信息为:" + mapString);
            }
        }
    }
}
