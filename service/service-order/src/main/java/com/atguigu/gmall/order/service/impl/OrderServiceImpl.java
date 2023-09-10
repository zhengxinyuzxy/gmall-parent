package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.gmall.cart.feign.CartFeign;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.util.GmallThreadLocalUtils;
import com.atguigu.gmall.pay.feign.AliPayFeign;
import com.atguigu.gmall.product.feign.ProductFeign;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private CartFeign cartFeign;

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ProductFeign productFeign;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 新增订单
     *
     * @param orderInfo
     */
    @Override
    public OrderInfo addOrder(OrderInfo orderInfo) {
        //参数校验
        if(orderInfo == null){
            throw new RuntimeException("参数错误!");
        }
        String userName = GmallThreadLocalUtils.getUserName();
        Long increment = redisTemplate.boundValueOps("cartInfo:" + userName).increment(1);
        if(increment > 1){
            throw new RuntimeException("重复下单!");
        }
        //补全订单表的数据
        Map<String, Object> addOrderInfo = cartFeign.getAddOrderInfo();
        //获取本次需要购买的购物车信息
        Object o = addOrderInfo.get("cartInfoList");
        List<JSONObject> cartInfoList =
                JSONObject.parseObject(o.toString(), List.class);
//        List<CartInfo> cartInfoList =
//                (List<CartInfo>)addOrderInfo.get("cartInfoList");
        //修改的情况
        if(orderInfo.getId() != null){
           //修改
            orderInfoMapper.updateById(orderInfo);
            //删除订单的详情数据
            orderDetailMapper.delete(
                    new LambdaQueryWrapper<OrderDetail>()
                            .eq(OrderDetail::getOrderId, orderInfo.getId()));
        }else{
            //获取总金额
            Double total =
                    Double.parseDouble(addOrderInfo.get("total").toString());
            orderInfo.setTotalAmount(new BigDecimal(total));
            //获取总数量
            Integer totalNum =
                    Integer.parseInt(addOrderInfo.get("totalNum").toString());

            //新增订单
            orderInfo.setOrderStatus(OrderStatus.UNPAID.getComment());
            orderInfo.setUserId("banzhang");
            orderInfo.setCreateTime(new Date());
            orderInfo.setExpireTime(new Date(System.currentTimeMillis() + 1800000));
            orderInfo.setProcessStatus(ProcessStatus.UNPAID.getComment());
            //新增订单完成后,就能获取到订单号了id
            orderInfoMapper.insert(orderInfo);
        }
        //扣减商品库存的记录
        Map<String, String> map = new ConcurrentHashMap<>();
        //新增订单详情
        List<OrderDetail> orderDetails = cartInfoList.stream().map(cartInfoJson -> {
            //反序列化
            CartInfo cartInfo = JSONObject.parseObject(cartInfoJson.toJSONString(), CartInfo.class);
            //初始化
            OrderDetail orderDetail = new OrderDetail();
            //补全部署
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setOrderId(orderInfo.getId());
            //新增
            orderDetailMapper.insert(orderDetail);
            //记录商品
            map.put(cartInfo.getSkuId() + "", cartInfo.getSkuNum() + "");
            //返回
            return orderDetail;
        }).collect(Collectors.toList());
        //存入order对象
        orderInfo.setOrderDetailList(orderDetails);
        //扣减库存
        if(!productFeign.decountStock(map)){
            throw new RuntimeException("新增订单失败,库存不足!");
        }
        //清除购物车
//        cartFeign.delCartInfo();
        //清除标识位:锁
        redisTemplate.delete("cartInfo:" + userName);
        //发送延迟消息,30分钟的有效期,防止用户一直不付钱,时间到了以后,超时自动取消订单
        rabbitTemplate.convertAndSend(
                "order_exchange",
                "order.delay",
                orderInfo.getId() + "",
                new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //获取消息的属性
                MessageProperties messageProperties = message.getMessageProperties();
                messageProperties.setExpiration("10000");
                return message;
            }
        });
        //返回结果
        return orderInfo;
    }

    @Autowired
    private AliPayFeign aliPayFeign;
    /**
     * 取消订单
     *
     * @param id
     */
    @Override
    public void cancleOrder(Long id, String msg) {
        //参数判断
        if(id == null){
            return;
        }
        //查询订单
        OrderInfo orderInfo = orderInfoMapper.selectById(id);
        if(orderInfo == null || orderInfo.getId() == null){
            return;
        }
        //幂等性问题的解决
        if(!orderInfo.getOrderStatus().equals(OrderStatus.UNPAID.getComment())){
            return;
        }
        //关闭交易
        aliPayFeign.closePay(id);
        //修改订单
        orderInfo.setOrderStatus(msg);
        orderInfo.setProcessStatus(msg);
        orderInfoMapper.updateById(orderInfo);
        //回滚商品库存的记录
        Map<String, String> map = new ConcurrentHashMap<>();
        //回滚库存
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>()
                        .eq(OrderDetail::getOrderId, id));
        orderDetails.stream().map(orderDetail -> {
            //记录需要回滚的商品的id和回滚的数量
            map.put(orderDetail.getSkuId() + "", orderDetail.getSkuNum() + "");
            return orderDetail;
        }).collect(Collectors.toList());
        //调用回滚
        if(!productFeign.rollbackStock(map)){
            throw new RuntimeException("取消失败,请重试!");
        }
    }

    /**
     * 修改订单
     *
     * @param map    :支付结果
     * @param status : 支付渠道: 1-微信 0-支付宝
     */
    @Override
    public void updateOrder(Map<String, String> map, Integer status) {
        if(status == 1){
            //微信修改逻辑
            wxPayUpdate(map);
        }else{
            //支付宝修改逻辑
            aliPayUpdate(map);
        }
    }

    /**
     * 支付宝支付的修改逻辑
     * @param map
     */
    private void aliPayUpdate(Map<String, String> map) {
        if(map.get("trade_status").equals("TRADE_SUCCESS")){
            //获取订单的信息从数据库
            String orderId = map.get("out_trade_no");
            //数据库查询
            OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
            if(orderInfo == null || orderInfo.getId() == null){
                return;
            }
            //防止幂等性问题
            if(!orderInfo.getOrderStatus().equals(OrderStatus.UNPAID.getComment())){
                return;
            }
            //修改订单的状态
            orderInfo.setOrderStatus(OrderStatus.PAID.getComment());
            orderInfo.setProcessStatus(ProcessStatus.PAID.getComment());
            orderInfo.setOutTradeNo(map.get("trade_no"));
            orderInfo.setTradeBody(JSONObject.toJSONString(map));
            orderInfoMapper.updateById(orderInfo);
        }
    }

    /**
     * 微信支付的修改订单逻辑
     * @param map
     */
    private void wxPayUpdate(Map<String, String> map){
        if(map.get("return_code").equals("SUCCESS") &&
                map.get("result_code").equals("SUCCESS")){
            //获取订单的信息从数据库
            String orderId = map.get("out_trade_no");
            //数据库查询
            OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
            if(orderInfo == null || orderInfo.getId() == null){
                return;
            }
            //防止幂等性问题
            if(!orderInfo.getOrderStatus().equals(OrderStatus.UNPAID.getComment())){
                return;
            }
            //修改订单的状态
            orderInfo.setOrderStatus(OrderStatus.PAID.getComment());
            orderInfo.setProcessStatus(ProcessStatus.PAID.getComment());
            orderInfo.setOutTradeNo(map.get("transaction_id"));
            orderInfo.setTradeBody(JSONObject.toJSONString(map));
            orderInfoMapper.updateById(orderInfo);
        }
    }
}
