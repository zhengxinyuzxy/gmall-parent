package com.atguigu.gmall.payment.service;

import java.util.Map;

public interface WxPayService {

    /**
     * 获取支付的二维码地址
     * @param paramMap
     * @return
     */
    public Map<String, String> getPayUrl(Map<String, String> paramMap);

    /**
     * 查询订单的支付结果
     * @param orderId
     * @return
     */
    public Map<String, String> getPayResult(Long orderId);
}
