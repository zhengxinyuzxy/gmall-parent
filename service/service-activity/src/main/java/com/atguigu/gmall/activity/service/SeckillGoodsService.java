package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.Date;
import java.util.List;

public interface SeckillGoodsService {

    /**
     * 获取时间菜单
     * @return
     */
    public List<Date> getDateList();


    /**
     * 查询时间段的秒杀商品列表
     * @param time
     * @return
     */
    public List<SeckillGoods> getSeckillGoods(String time);

    /**
     * 获取商品的详情
     * @param time
     * @param id
     * @return
     */
    public SeckillGoods getSeckillGoods(String time, String id);

    /**
     * 商品活动结束后,将redis中的商品库存数据写入数据库中去
     * @param seckillGoods
     */
    public void udpateSeckillGoods(List<SeckillGoods> seckillGoods, String key);
}
