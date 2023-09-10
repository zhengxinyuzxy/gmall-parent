package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.DateUtil;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class SeckillGoodsServiceImpl implements SeckillGoodsService {
    /**
     * 获取时间菜单
     *
     * @return
     */
    @Override
    public List<Date> getDateList() {
        return DateUtil.getDateMenus();
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 查询时间段的秒杀商品列表
     *
     * @param time
     * @return
     */
    @Override
    public List<SeckillGoods> getSeckillGoods(String time) {
        List<SeckillGoods> values = redisTemplate.boundHashOps(time).values();
        return values;
    }

    /**
     * 获取商品的详情
     *
     * @param time
     * @param id
     * @return
     */
    @Override
    public SeckillGoods getSeckillGoods(String time, String id) {
        SeckillGoods o = (SeckillGoods)redisTemplate.boundHashOps(time).get(id);
        return o;
    }

    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;
    /**
     * 商品活动结束后,将redis中的商品库存数据写入数据库中去
     *
     * @param seckillGoods
     */
    @Override
    public void udpateSeckillGoods(List<SeckillGoods> seckillGoods, String key) {
        //将数据写入到数据库中去
        seckillGoods.stream().map(goods -> {
            seckillGoodsMapper.updateById(goods);
            return goods;
        }).collect(Collectors.toList());
        //清理redis中该时间段的商品的数据
        redisTemplate.delete(key);
    }
}
