package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillOrderService;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/seckill/order")
public class SeckillOrderController {

    @Autowired
    private SeckillOrderService seckillOrderService;


    /**
     * 秒杀排队
     * @param time
     * @param goodsId
     * @param num
     * @return
     */
    @GetMapping(value = "/addOrder")
    public Result addOrder(String time, String goodsId, Integer num){
        return Result.ok(seckillOrderService.addOrder(time, goodsId, num));
    }


    /**
     * 主动取消订单
     * @param id
     * @return
     */
    @GetMapping(value = "/cancleSeckillOrder")
    public Result cancleSeckillOrder(String id){
        //获取用户名
        String username = "banzhang";
        seckillOrderService.cancleSeckillOrder(id, 3, username);
        return Result.ok();
    }
}
