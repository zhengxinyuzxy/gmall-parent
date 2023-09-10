package com.atguigu.gmall.activity.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.mapper.SeckillOrderMapper;
import com.atguigu.gmall.activity.pojo.SeckillOrder;
import com.atguigu.gmall.activity.pojo.UserRecode;
import com.atguigu.gmall.activity.service.SeckillOrderService;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 秒杀下单: 秒杀同步排队
     *  @param time
     * @param goodsId
     * @param num
     * @return
     */
    @Override
    public UserRecode addOrder(String time, String goodsId, Integer num) {
        String username = "banzhang";
        UserRecode userRecode = new UserRecode();
        //参数校验
        if(StringUtils.isEmpty(time) || StringUtils.isEmpty(goodsId)){
            throw new RuntimeException("参数错误!");
        }
        //防止重复排队
        Long userRecordCount = redisTemplate.boundHashOps("User_Record_count").increment(username, 1);
        if(userRecordCount > 1){
            userRecode.setMsg("秒杀失败,重复排队!");
            userRecode.setStatus(1);
            return userRecode;
        }
        //记录用户的排队信息到redis中去
        userRecode.setMsg("排队中!");
        userRecode.setStatus(1);
        userRecode.setCreateTime(new Date());
        userRecode.setGoodsId(goodsId);
        userRecode.setTime(time);
        userRecode.setUsername(username);
        userRecode.setNum(num);
        redisTemplate.boundHashOps("Seckill_User_Recode").put(username, userRecode);
        //发送下单消息
        rabbitTemplate.convertAndSend("seckill_order_exchange",
                "seckill.order.add",
                JSONObject.toJSONString(userRecode));
        //返回用户排队成功
        return userRecode;
    }

    @Resource
    private SeckillOrderMapper seckillOrderMapper;
    /**
     * 取消订单
     *
     * @param id
     * @param status
     */
    @Override
    public void cancleSeckillOrder(String id, Integer status, String username) {

        //查询订单是否存在
        SeckillOrder seckillOrder = (SeckillOrder)redisTemplate.boundHashOps("User_Seckill_Order").get(username);
        //判断订单的前置状态
        if(seckillOrder == null){
            return;
        }
        //只修改未支付的订单
        if(!seckillOrder.getStatus().equals("0")){
            return;
        }
        //取消订单,数据同步到数据库
        seckillOrder.setStatus(status + "");
        seckillOrderMapper.insert(seckillOrder);
        //回滚库存

        rollbackSeckillGoodsStock(username, seckillOrder.getNum());
        //删除redis中排队的信息
        redisTemplate.boundHashOps("Seckill_User_Recode").delete(username);
        //清除用户的排队计数
        redisTemplate.boundHashOps("User_Record_count").delete(username);
        //删除redis中订单的信息
        redisTemplate.boundHashOps("User_Seckill_Order").delete(username);
    }

    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;
    /**
     * 回滚库存
     * @param username
     */
    private void rollbackSeckillGoodsStock(String username, Integer num) {
        //获取用户的排队信息
        UserRecode userRecode =
                (UserRecode)redisTemplate.boundHashOps("Seckill_User_Recode").get(username);
        //获取商品id
        String goodsId = userRecode.getGoodsId();
        //获取时间段
        String time = userRecode.getTime();
        //判断商品是否活动已经过期了
        SeckillGoods seckillGoods =
                (SeckillGoods) redisTemplate.boundHashOps(time).get(goodsId + "");
        if(seckillGoods == null){
            //活动过期,回滚数据库的数据
            seckillGoodsMapper.rollBackSeckillGoodsStock(seckillGoods.getId(), num);
        }else{
            //活动没过期,同步redis的数据
            String[] ids = getIds(num, goodsId);
            redisTemplate.boundListOps("Seckill_Goods_Stock_List_" + goodsId).leftPushAll(ids);
            //回滚库存的自增值
            Long stockCount = redisTemplate.boundHashOps("Seckill_Goods_Stock_Show").increment(goodsId + "", num);
            //更新一下redis中商品的数据
            seckillGoods.setStockCount(stockCount.intValue());
            redisTemplate.boundHashOps(time).put(goodsId + "", seckillGoods);
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

    /**
     * 修改秒杀订单的支付结果
     *
     * @param msg
     */
    @Override
    public void updatePayStatus(String msg) {
        //将报文反序列化
        Map<String,String> map = JSONObject.parseObject(msg, Map.class);
        //获取订单号
        String outTradeNo = map.get("out_trade_no");
        //获取附加参数
        String passbackParams = map.get("passback_params");
        Map<String,String> paramMap = JSONObject.parseObject(passbackParams, Map.class);
        String username = paramMap.get("username");
        //修改状态
        SeckillOrder seckillOrder =
                (SeckillOrder)redisTemplate.boundHashOps("User_Seckill_Order").get(username);
        if(seckillOrder == null){
            return;
        }
        //幂等性问题
        if(!seckillOrder.getStatus().equals("0")){
            return;
        }
        //确认数据库中是否存在数据
        SeckillOrder seckillOrderDB = seckillOrderMapper.selectById(seckillOrder.getId());
        if(seckillOrderDB != null && seckillOrderDB.getId() != null){
            return;
        }
        //修改订单的状态
        seckillOrder.setStatus("1");
        seckillOrderMapper.insert(seckillOrder);
        //删除redis中排队的信息
        redisTemplate.boundHashOps("Seckill_User_Recode").delete(username);
        //清除用户的排队计数
        redisTemplate.boundHashOps("User_Record_count").delete(username);
        //删除redis中订单的信息
        redisTemplate.boundHashOps("User_Seckill_Order").delete(username);
    }
}
