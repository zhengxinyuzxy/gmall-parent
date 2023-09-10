package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 平台属性名称的mapper映射
 */
@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {

    /**
     * 根据分类信息查询平台属性的列表
     * @param category1
     * @param category2
     * @param category3
     * @return
     */
    public List<BaseAttrInfo> selectBaseAttrInfoByCategory(@Param("category1") Long category1,
                                                           @Param("category2") Long category2,
                                                           @Param("category3") Long category3);

    /**
     * 根据sku的id查询销售属性列表
     * @param skuId
     * @return
     */
    public List<BaseAttrInfo> selectSkuBaseAttrInfoBySkuId(@Param("skuId") Long skuId);
}
