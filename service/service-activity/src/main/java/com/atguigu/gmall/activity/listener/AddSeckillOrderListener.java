package com.atguigu.gmall.activity.listener;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.gmall.activity.pojo.SeckillOrder;
import com.atguigu.gmall.activity.pojo.UserRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * 秒杀异步下单的监听类
 */
@Component
public class AddSeckillOrderListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 秒杀异步下单的监听方法
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "seckill_order_queue")
    public void addSeckillOrder(Channel channel, Message message){
        //获取消息
        String msg = new String(message.getBody());
        //反序列化
        UserRecode userRecode = JSONObject.parseObject(msg, UserRecode.class);
        //获取商品id
        String goodsId = userRecode.getGoodsId();
        //获取用户名
        String username = userRecode.getUsername();
        //获取商品所在的时间段
        String time = userRecode.getTime();
        //商品的购买数量
        Integer num = userRecode.getNum();
//        //判断用户是否存在未支付的订单,若存在,直接下单失败!---由于重复排队问题的解决,不可能存在未支付的订单
//        Object o = redisTemplate.boundHashOps("User_Seckill_Order").get(username);
//        if(o != null){
//            //用户存在未支付订单
//            userRecode.setMsg("下单失败,存在未支付订单,请支付后重新购买!");
//            userRecode.setStatus(3);
//            //更新redis中排队的信息
//            redisTemplate.boundHashOps("Seckill_User_Recode").put(username, userRecode);
//            return;
//        }
        //获取商品的信息--从redis中
        SeckillGoods seckillGoods = (SeckillGoods)redisTemplate.boundHashOps(time).get(goodsId);
        //判断商品是否存在
        if(seckillGoods != null) {
            //从商品的list队列中进行取值
            int loop = 0;
            for (int i = 0; i < num; i++) {
                Object o = redisTemplate.boundListOps("Seckill_Goods_Stock_List_" + goodsId).rightPop();
                if(o == null){
                    //商品售罄了
                    userRecode.setMsg("下单失败,商品库存不足!");
                    userRecode.setStatus(3);
                    //更新redis中排队的信息
                    redisTemplate.boundHashOps("Seckill_User_Recode").put(username, userRecode);
                    //清除用户的排队计数
                    redisTemplate.boundHashOps("User_Record_count").delete(username);
                    //回滚
                    String[] ids = getIds(loop, goodsId);
                    redisTemplate.boundListOps("Seckill_Goods_Stock_List_" + goodsId).leftPushAll(ids);
                    return;
                }
                loop++;
            }
            //库存足够,生成订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(UUID.randomUUID().toString().replace("-", ""));
            seckillOrder.setGoodsId(goodsId);
            seckillOrder.setNum(num);
            seckillOrder.setMoney((seckillGoods.getCostPrice().doubleValue() * num) + "");
            seckillOrder.setUserId(username);
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setStatus("0");
            //将订单的信息存入redis中去
            redisTemplate.boundHashOps("User_Seckill_Order").put(username, seckillOrder);
            //扣减库存
            Long stockCount = redisTemplate.boundHashOps("Seckill_Goods_Stock_Show").increment(goodsId + "", -num);
            seckillGoods.setStockCount(stockCount.intValue());
            //更新redis中商品的库存数据
            redisTemplate.boundHashOps(time).put(goodsId, seckillGoods);
            //修改用户的排队状态
            userRecode.setMsg("秒杀下单成功!");
            userRecode.setStatus(2);
            userRecode.setOrderId(seckillOrder.getId());
            userRecode.setMoney(seckillOrder.getMoney());
            redisTemplate.boundHashOps("Seckill_User_Recode").put(username, userRecode);
            //延迟消息---TODO
            rabbitTemplate.convertAndSend("seckill_delay_exchange",
                    "seckill.timeout.order",
                    JSONObject.toJSONString(seckillOrder),
                    new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    MessageProperties messageProperties = message.getMessageProperties();
                    messageProperties.setExpiration("600000");
                    return message;
                }
            });
            return;
        }

    }

    private String[] getIds(Integer stockCount, String goodsId){
        String[] ids = new String[stockCount];
        //循环复制
        for (int i = 0; i< stockCount; i++) {
            ids[i] = goodsId;
        }
        return ids;
    }
}
