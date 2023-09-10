package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/cart")
public class CartInfoController {

    @Autowired
    private CartInfoService cartInfoService;

    /**
     * 新增购物车
     * @param num
     * @param skuId
     * @return
     */
    @GetMapping(value = "/addCart")
    public Result addCart(Integer num, Long skuId){
        cartInfoService.addCartInfo(skuId, num);
        return Result.ok();
    }

    /**
     * 查询购物车数据
     * @return
     */
    @GetMapping(value = "/getCartInfo")
    public Result getCartInfo(){
        List<CartInfo> cartInfoList = cartInfoService.getCartInfo();
        return Result.ok(cartInfoList);
    }


    /**
     * 删除购物车数据
     * @param id
     * @return
     */
    @GetMapping(value = "/delCartInfo")
    public Result delCartInfo(Long id){
        cartInfoService.del(id);
        return Result.ok();
    }

    /**
     * 修改选中状态
     * @param id
     * @param status
     * @return
     */
    @GetMapping(value = "/changeChecked")
    public Result changeChecked(Long id, Integer status){
        cartInfoService.changeChecked(id, status);
        return Result.ok();
    }

    /**
     * 合并购物车
     * @param cartInfoList
     * @return
     */
    @PostMapping(value = "/mergeCartInfo")
    public Result mergeCartInfo(@RequestBody List<CartInfo> cartInfoList){
        cartInfoService.mergeCartInfo(cartInfoList);
        return Result.ok();
    }

    /**
     * 查询订单确认页面的购物车详情和总金额总数量
     * @return
     */
    @GetMapping(value = "/getOrderConfirm")
    public Result getOrderConfirm(){
        return Result.ok(cartInfoService.getOrderConfirm());
    }

    /**
     * 下单使用的查询接口
     * @return
     */
    @GetMapping(value = "/getAddOrderInfo")
    public Map<String, Object> getAddOrderInfo(){
        return cartInfoService.getOrderConfirm();
    }

    /**
     * 下单完成后清空当前购买的商品的购物车数据
     * @return
     */
    @GetMapping(value = "/delCart")
    public Boolean delCart(){
        return cartInfoService.delCartInfo();
    }
}
