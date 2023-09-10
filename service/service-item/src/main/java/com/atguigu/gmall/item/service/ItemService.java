package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * 商品详情页的服务类接口
 */
public interface ItemService {

    /**
     * 获取商品详情的数据
     * @param skuId
     * @return
     */
    public Map<String, Object> getSkuItem(Long skuId);
}
