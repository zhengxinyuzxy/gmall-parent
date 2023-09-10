package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;
import java.util.Map;

public interface CartInfoService {

    /**
     * 新增购物车
     * @param skuId
     * @param num
     */
    public void addCartInfo(Long skuId, Integer num);

    /**
     * 查询购物车数据
     * @return
     */
    public List<CartInfo> getCartInfo();

    /**
     * 删除购物车
     * @param id
     * @return
     */
    public List<CartInfo> del(Long id);


    /**
     * 修改选中状态
     * @param id
     * @param status
     * @return
     */
    public void changeChecked(Long id, Integer status);


    /**
     * 合并购物车
     * @param cartInfos
     */
    public void mergeCartInfo(List<CartInfo> cartInfos);


    /**
     * 查询订单确认页的购物车信息
     * @return
     */
    public Map<String,Object> getOrderConfirm();

    /**
     * 清空购物车
     * @return
     */
    public Boolean delCartInfo();
}
