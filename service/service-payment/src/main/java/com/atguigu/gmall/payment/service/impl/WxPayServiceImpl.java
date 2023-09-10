package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.gmall.payment.service.WxPayService;
import com.atguigu.gmall.payment.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class WxPayServiceImpl implements WxPayService {

    @Value("${weixin.pay.appid}")
    private String appId;
    @Value("${weixin.pay.partner}")
    private String partner;
    @Value("${weixin.pay.partnerkey}")
    private String partnerkey;
    @Value("${weixin.pay.notifyUrl}")
    private String notifyUrl;

    /**
     * 获取支付的二维码地址
     *  @param paramMap
     * @return
     */
    @Override
    public Map<String, String> getPayUrl(Map<String, String> paramMap) {
        //参数校验
        if(paramMap.get("orderId") == null ||
                paramMap.get("money") == null ||
                StringUtils.isEmpty(paramMap.get("desc"))){
            return null;
        }
        //包装请求参数,并且转换文xml格式
        Map<String, String> param = new HashMap<>();
        param.put("appid", appId);
        param.put("mch_id", partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("body", paramMap.get("desc"));
        param.put("out_trade_no", paramMap.get("orderId"));
        param.put("total_fee", paramMap.get("money"));
        param.put("spbill_create_ip", "192.168.200.1");
        param.put("notify_url", notifyUrl);
        param.put("trade_type", "NATIVE");
        param.put("attach", JSONObject.toJSONString(paramMap));
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //发起post请求,请求微信的支付服务
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            //发起请求
            httpClient.post();
            //获取返回结果
            String content = httpClient.getContent();
            //解析xml格式的返回结果
            Map<String, String> map = WXPayUtil.xmlToMap(content);
            //获取状态
            if(map.get("return_code").equals("SUCCESS") && map.get("result_code").equals("SUCCESS")){
                //获取二维码的地址
                return map;
        }
            //返回
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询订单的支付结果
     *
     * @param orderId
     * @return
     */
    @Override
    public Map<String, String> getPayResult(Long orderId) {
        //参数校验
        if(orderId == null){
            return null;
        }
        //包装请求参数,并且转换文xml格式
        Map<String, String> param = new HashMap<>();
        param.put("appid", appId);
        param.put("mch_id", partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("out_trade_no", orderId + "");
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //发起post请求,请求微信的支付服务
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            //发起请求
            httpClient.post();
            //获取返回结果
            String content = httpClient.getContent();
            //解析xml格式的返回结果
            Map<String, String> map = WXPayUtil.xmlToMap(content);
            //获取状态
            if(map.get("return_code").equals("SUCCESS") && map.get("result_code").equals("SUCCESS")){
                //获取支付结果
                return map;
            }
            //返回
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
