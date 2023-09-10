package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * sku的销售属性值的mapper映射
 */
@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {

    /**
     * 根据spu的id查询该spu下所有sku的销售属性的键值对
     * @param spuId
     * @return
     */
    @Select("SELECT sku_id,GROUP_CONCAT( DISTINCT sale_attr_value_id ORDER BY sale_attr_value_id SEPARATOR ',' ) AS value_ids " +
            "FROM sku_sale_attr_value " +
            "WHERE spu_id = #{spuId} " +
            "GROUP BY sku_id")
    public List<Map> selectSkuSaleValueKeys(@Param("spuId") Long spuId);
}
