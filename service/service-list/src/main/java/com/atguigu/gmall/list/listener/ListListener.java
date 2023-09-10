package com.atguigu.gmall.list.listener;

import com.atguigu.gmall.list.service.ListService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 搜索微服务数据同步的监听类
 */
@Component
@Log4j2
public class ListListener {
    @Autowired
    private ListService listService;

    /**
     * 监听商品上架
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "sku_up_queue")
    public void skuDataListenerUp(Channel channel, Message message){
        updateSkuInfo(channel, message, 1);
    }

    /**
     * 监听商品下架
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "sku_down_queue")
    public void skuDataListenerDown(Channel channel, Message message){
        updateSkuInfo(channel, message, 0);
    }

    private void updateSkuInfo(Channel channel, Message message, Integer status){
        //获取消息
        byte[] body = message.getBody();
        String idString = new String(body);
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //消息的编号
        long deliveryTag = messageProperties.getDeliveryTag();
        try {
            if(status == 1){
                //修改商品的上架状态
                listService.goodsUpper(Long.parseLong(idString));
            }else{
                //修改商品的上架状态
                listService.goodsDown(Long.parseLong(idString));
            }
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //确认消息是否被消费过
                if(messageProperties.isRedelivered()){
                    //如果已经消费过,记录到日志中去
                    log.error("商品同步失败,商品的id为:" + idString);
                    channel.basicReject(deliveryTag, false);
                    return;
                }
                //再试一次
                channel.basicReject(deliveryTag, true);
            }catch (Exception e1){
                log.error("商品同步失败,商品的id为:" + idString);
            }
        }
    }
}
