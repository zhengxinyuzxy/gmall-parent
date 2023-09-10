package com.atguigu.gmall.activity.listener;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.gmall.activity.pojo.SeckillOrder;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品活动时间结束后同步数据的监听类
 */
@Component
@Log4j2
public class SeckillGoodsTimeOutListener {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillGoodsService seckillGoodsService;

    /**
     * 商品活动时间结束后同步数据的监听类
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "seckill_goods_timeout_queue")
    public void cancleSeckillTimeOurOrder(Channel channel, Message message){
        //获取消息
        byte[] body = message.getBody();
        String key = new String(body);
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //消息的编号
        long deliveryTag = messageProperties.getDeliveryTag();
        try {
            //同步商品数据
            List<SeckillGoods> seckillGoods = redisTemplate.boundHashOps(key).values();
            seckillGoodsService.udpateSeckillGoods(seckillGoods, key);
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //确认消息是否被消费过
                if(messageProperties.isRedelivered()){
                    //如果已经消费过,记录到日志中去
                    log.error("超时商品清理失败,时间段的内容为:" + key);
                    channel.basicReject(deliveryTag, false);
                    return;
                }
                //再试一次
                channel.basicReject(deliveryTag, true);
            }catch (Exception e1){
                log.error("超时商品清理失败,时间段的内容为:" + key);
            }
        }

    }
}
