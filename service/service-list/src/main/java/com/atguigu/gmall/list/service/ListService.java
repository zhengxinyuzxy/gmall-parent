package com.atguigu.gmall.list.service;

public interface ListService {

    /**
     * es中商品上架
     * @param skuId
     */
    public void goodsUpper(Long skuId);

    /**
     * es中商品下架
     * @param skuId
     */
    public void goodsDown(Long skuId);

    /**
     * 增加热度值
     * @param skuId
     */
    public void addScore(Long skuId);
}
