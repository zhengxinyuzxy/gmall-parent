package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/seckill/goods")
public class SeckillController {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    /**
     * 获取时间菜单
     * @return
     */
    @GetMapping(value = "/getDateList")
    public Result getDateList(){
        return Result.ok(seckillGoodsService.getDateList());
    }


    /**
     * 查询时间段的秒杀商品列表
     * @param time
     * @return
     */
    @GetMapping(value = "/getSeckillGoods")
    public Result getSeckillGoods(String time){
        return Result.ok(seckillGoodsService.getSeckillGoods(time));
    }

    /**
     * 获取商品的详情
     * @param time
     * @param id
     * @return
     */
    @GetMapping(value = "/getSeckillGoodsDetail")
    public Result getSeckillGoods(String time, String id){
        return Result.ok(seckillGoodsService.getSeckillGoods(time, id));
    }

}
