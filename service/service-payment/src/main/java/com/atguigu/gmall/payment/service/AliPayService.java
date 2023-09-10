package com.atguigu.gmall.payment.service;

import java.util.Map;

public interface AliPayService {


    /**
     * 支付宝支付的下单页面接口
     * @param paramMap
     * @return
     */
    public String getPayPage(Map<String,String> paramMap);


    /**
     * 查询支付宝支付结果
     * @param orderId
     */
    public String getPayResult(Long orderId);

    /**
     * 关闭交易
     * @param orderId
     * @return
     */
    public String closePay(Long orderId);
}
