package com.atguigu.gmall.activity.task;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.util.DateUtil;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀定时任务:将数据库中的数据定时的存入redis中去
 */
@Component
public class SeckillGoodsIntoRedisTask {

    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 每20秒执行一次定时任务
     */
    @Scheduled(cron = "0/20 * * * * *")
    public void goodsFromDBtoRedis(){
        //计算当前系统的时间,和时间段,计算出当前时间所属的时间段以及后面4个时间段一共5个时间段
        List<Date> dateMenus = DateUtil.getDateMenus();
        //每个时间段查询该时间段的活动商品
        for (Date dateMenu : dateMenus) {
            //获取时间段的开始时间: 2021-10-29 10:00
            String startTime = DateUtil.data2str(dateMenu, DateUtil.PATTERN_YYYY_MM_DDHHMM);
            //获取redis存储的时间段的key: 2021102910
            String key = DateUtil.data2str(dateMenu, DateUtil.PATTERN_YYYYMMDDHH);
            //计算活动的截止时间: 2021-10-29 12:00
            Date date = DateUtil.addDateHour(dateMenu, 2);
            //发送商品数据同步的消息
            sendTimeOutMessage(key, date);
            String endTime = DateUtil.data2str(date, DateUtil.PATTERN_YYYY_MM_DDHHMM);
            //去数据库中查询商品的数据
            LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
            //审核通过的商品
            wrapper.eq(SeckillGoods::getStatus, "1");
            //在活动时间以内: startTime<=   <endTime
            wrapper.ge(SeckillGoods::getStartTime, startTime);
            wrapper.le(SeckillGoods::getEndTime, endTime);
            //库存大于0
            wrapper.gt(SeckillGoods::getStockCount, 0);
            //redis中没有的
            Set keys = redisTemplate.boundHashOps(key).keys();
            if(keys != null && keys.size() > 0){
                wrapper.notIn(BaseEntity::getId, keys);
            }
            //数据库查询秒杀商品列表
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(wrapper);
            //将商品存入redis中去
            for (SeckillGoods seckillGood : seckillGoods) {
                redisTemplate.boundHashOps(key).put(seckillGood.getId() + "", seckillGood);
                //构建一个商品库存个数的长度的队列
                String[] ids = getIds(seckillGood.getStockCount(), seckillGood.getId() + "");
                redisTemplate.boundListOps("Seckill_Goods_Stock_List_" + seckillGood.getId()).leftPushAll(ids);
                //构建一个商品库存的自增对象
                redisTemplate.boundHashOps("Seckill_Goods_Stock_Show").increment(seckillGood.getId() + "", seckillGood.getStockCount());
            }
        }
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 商品活动到期后需要同步数据的延迟消息发送
     * @param date
     */
    private void sendTimeOutMessage(String key, Date date) {
        //判断是否已经发送过了
        Long message = redisTemplate.boundHashOps("seckill_good_clear_message").increment(key, 1);
        if(message > 1){
            return;
        }
        redisTemplate.expire("seckill_good_clear_message", 24, TimeUnit.HOURS);
        //计算商品的存活时间
        long liveTime = date.getTime() - System.currentTimeMillis();
        //发送延迟消息
        rabbitTemplate.convertAndSend("seckill_goods_time_out_exchange",
                "seckill.goods.time.out",
                key,
                new MessagePostProcessor() {
                    @Override
                    public Message postProcessMessage(Message message) throws AmqpException {
                        MessageProperties messageProperties = message.getMessageProperties();
                        messageProperties.setExpiration(liveTime + "");
                        return message;
                    }
                });
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
