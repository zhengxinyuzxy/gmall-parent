package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.activity.pojo.UserRecode;


public interface SeckillOrderService {

    /**
     * 秒杀下单: 秒杀同步排队
     * @param time
     * @param goodsId
     * @param num
     * @return
     */
    public UserRecode addOrder(String time, String goodsId, Integer num);

    /**
     * 取消订单
     * @param id
     * @param status
     */
    public void cancleSeckillOrder(String id, Integer status, String username);

    /**
     * 修改秒杀订单的支付结果
     * @param msg
     */
    public void updatePayStatus(String msg);
}
