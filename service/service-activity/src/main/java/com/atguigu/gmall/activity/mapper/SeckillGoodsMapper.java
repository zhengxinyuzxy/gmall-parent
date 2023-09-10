package com.atguigu.gmall.activity.mapper;

import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 秒杀商品表的mapper映射
 */
@Mapper
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    @Update("update seckill_goods set stock_count = stock_count + #{num} where id = #{id}")
    public int rollBackSeckillGoodsStock(@Param("id") Long id,
                                         @Param("num") Integer num);
}
