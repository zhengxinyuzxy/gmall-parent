package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * skuinfo表的mapper映射
 */
@Mapper
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    /**
     * 扣减库存
     * @param id
     * @param num
     * @return
     */
    @Update("update sku_info set stock=stock - #{num} where id = #{id} and stock >= #{num}")
    public int decountStock(@Param("id") Long id,
                            @Param("num") Integer num);

    /**
     * 回滚库存
     * @param id
     * @param num
     * @return
     */
    @Update("update sku_info set stock=stock + #{num} where id = #{id}")
    public int rollbackStock(@Param("id") Long id,
                            @Param("num") Integer num);
}
