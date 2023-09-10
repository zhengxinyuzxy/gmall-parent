package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * spu的销售属性名称表
 */
@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    /**
     * 根据spu的id查询该spu的所有的销售属性名称和值的列表
     * @param spuId
     * @return
     */
    public List<SpuSaleAttr> selectSpuSaleAttrBySpuId(@Param("spuId") Long spuId);

    /**
     * 根据spu和sku的id查询所有的销售属性和销售属性值,并且标注出当前的sku是哪个
     * @param spuId
     * @param skuId
     * @return
     */
    public List<SpuSaleAttr> selectSpuSaleAttrBySpuIdAndSkuId(@Param("spuId") Long spuId,
                                                              @Param("skuId") Long skuId);
}
