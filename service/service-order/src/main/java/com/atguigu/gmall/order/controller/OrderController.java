package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.util.GmallThreadLocalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 新增订单
     * @param orderInfo
     * @return
     */
    @PostMapping(value = "/addOrder")
    public Result addOrder(@RequestBody OrderInfo orderInfo){

        return Result.ok(orderService.addOrder(orderInfo));
    }

    /**
     * 主动取消订单
     * @param id
     * @return
     */
    @GetMapping(value = "/cancleOrder/{id}")
    public Result cancleOrder(@PathVariable(value = "id") Long id){
        orderService.cancleOrder(id, OrderStatus.CANCLE.getComment());
        return Result.ok();
    }
}
