package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.payment.service.AliPayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {

    @Value("${alipay_url}")
    private String alipayUrl;
    @Value("${app_id}")
    private String appId;
    @Value("${app_private_key}")
    private String appPrivateKey;
    @Value("${alipay_public_key}")
    private String alipayPublicKey;
    @Value("${return_payment_url}")
    private String returnPaymentUrl;
    @Value("${notify_payment_url}")
    private String notifyPaymentUrl;
    /**
     * 支付宝支付的下单页面接口
     *
     * @param paramMap
     * @return
     */
    @Override
    public String getPayPage(Map<String,String> paramMap) {
        //初始化阿里支付的客户端对象
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayUrl,
                appId,
                appPrivateKey,
                "json",
                "UTF-8",
                alipayPublicKey,
                "RSA2");
        //初始化统一下单页面接口的request对象
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        //异步通知地址设置
        request.setNotifyUrl(notifyPaymentUrl);
        //同步跳转地址设置
        request.setReturnUrl(returnPaymentUrl);
        //包装请求参数
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", paramMap.get("orderId"));
        bizContent.put("total_amount", paramMap.get("money"));
        bizContent.put("subject", paramMap.get("desc"));
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContent.put("passback_params", JSONObject.toJSONString(paramMap));
        request.setBizContent(bizContent.toString());
        try{
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if(response.isSuccess()){
                String body = response.getBody();
                return body;
            } else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询支付宝支付结果
     *
     * @param orderId
     */
    @Override
    public String getPayResult(Long orderId) {
        //初始化阿里支付的客户端对象
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayUrl,
                appId,
                appPrivateKey,
                "json",
                "UTF-8",
                alipayPublicKey,
                "RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId + "");
        request.setBizContent(bizContent.toString());
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if(response.isSuccess()){
                String body = response.getBody();
                return body;
            } else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 关闭交易
     *
     * @param orderId
     * @return
     */
    @Override
    public String closePay(Long orderId) {
        //初始化阿里支付的客户端对象
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayUrl,
                appId,
                appPrivateKey,
                "json",
                "UTF-8",
                alipayPublicKey,
                "RSA2");
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId + "");
        request.setBizContent(bizContent.toString());
        try {
            AlipayTradeCloseResponse response = alipayClient.execute(request);
            if(response.isSuccess()){
                return response.getBody();
            } else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
