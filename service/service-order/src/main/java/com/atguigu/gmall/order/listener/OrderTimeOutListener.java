package com.atguigu.gmall.order.listener;

import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 超时未支付的订单的监听类
 */
@Component
@Log4j2
public class OrderTimeOutListener {

    @Autowired
    private OrderService orderService;

    /**
     * 超时未支付订单的监听类
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "order_queue")
    public void cancleListener(Channel channel, Message message){
        //获取消息
        byte[] body = message.getBody();
        String idString = new String(body);
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //消息的编号
        long deliveryTag = messageProperties.getDeliveryTag();
        try {
            //取消超时订单
            orderService.cancleOrder(Long.parseLong(idString), OrderStatus.TIME_OUT.getComment());
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //确认消息是否被消费过
                if(messageProperties.isRedelivered()){
                    //如果已经消费过,记录到日志中去
                    log.error("订单取消失败,订单的id为:" + idString);
                    channel.basicReject(deliveryTag, false);
                    return;
                }
                //再试一次
                channel.basicReject(deliveryTag, true);
            }catch (Exception e1){
                log.error("订单取消失败,订单的id为:" + idString);
            }
        }
    }
}
