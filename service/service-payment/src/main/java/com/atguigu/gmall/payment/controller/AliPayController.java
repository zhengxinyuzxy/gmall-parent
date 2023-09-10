package com.atguigu.gmall.payment.controller;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.gmall.payment.service.AliPayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/ali/pay")
public class AliPayController {

    @Autowired
    private AliPayService aliPayService;

    /**
     * 获取支付二维码地址
     * @param paramMap
     * @return
     */
    @GetMapping(value = "/getPayPage")
    public String getPayUrl(@RequestParam Map<String, String> paramMap){
        return aliPayService.getPayPage(paramMap);
    }

    /**
     * 主动查询支付的结果
     * @param orderId
     * @return
     */
    @GetMapping(value = "/getPayResult")
    public String getPayResult(Long orderId){
        return aliPayService.getPayResult(orderId);
    }


    /**
     * 同步回调
     * @param map
     * @return
     */
    @RequestMapping(value = "/callbackUrl")
    public String callbackUrl(@RequestParam Map<String,String> map){
        System.out.println(map);
        return "同步回调成功!!!";
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 同步回调
     * @param map
     * @return
     */
    @RequestMapping(value = "/notifyUrl")
    public String notifyUrl(@RequestParam Map<String,String> map){
        String passbackParams = map.get("passback_params");
        Map<String, String> assback = JSONObject.parseObject(passbackParams, Map.class);
        //发消息
        rabbitTemplate.convertAndSend(assback.get("exchange"),assback.get("routingKey"), JSONObject.toJSONString(map));
        return "success";
    }

    /**
     * 关闭交易
     * @param orderId
     * @return
     */
    @GetMapping(value = "/closePay/{orderId}")
    public String closePay(@PathVariable(value = "orderId") Long orderId){
        return aliPayService.closePay(orderId);
    }
}
