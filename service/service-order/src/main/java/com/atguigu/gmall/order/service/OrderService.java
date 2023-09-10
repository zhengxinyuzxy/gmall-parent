package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

import java.util.Map;

public interface OrderService {

    /**
     * 新增订单
     * @param orderInfo
     */
    public OrderInfo addOrder(OrderInfo orderInfo);

    /**
     * 取消订单
     * @param id
     */
    public void cancleOrder(Long id, String msg);

    /**
     * 修改订单
     * @param map:支付结果
     * @param status: 支付渠道: 1-微信 0-支付宝
     */
    void updateOrder(Map<String, String> map, Integer status);
}
