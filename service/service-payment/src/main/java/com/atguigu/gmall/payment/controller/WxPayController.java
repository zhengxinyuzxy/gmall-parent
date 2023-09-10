package com.atguigu.gmall.payment.controller;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.gmall.payment.service.WxPayService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/wx/pay")
public class WxPayController {

    @Autowired
    private WxPayService wxPayService;

    /**
     * 获取支付二维码地址
     * @param paramMap
     * @return
     */
    @GetMapping(value = "/getPayUrl")
    public Map getPayUrl(@RequestParam Map<String, String> paramMap){
        return wxPayService.getPayUrl(paramMap);
    }

    /**
     * 主动查询支付的结果
     * @param orderId
     * @return
     */
    @GetMapping(value = "/getPayResult")
    public Map getPayResult(Long orderId){
        return wxPayService.getPayResult(orderId);
    }


    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 微信通知支付结果的回调接口
     * @return
     */
    @RequestMapping(value = "/notify")
    public String wxNotify(HttpServletRequest request) throws Exception{
        //获取微信通知的结果:数据流
        ServletInputStream inputStream = request.getInputStream();
        //转换为输出流
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        //定义缓冲区
        byte[] buffer = new byte[1024];
        int len = 0;
        //读取数据
        while ((len=inputStream.read(buffer)) != -1){
            os.write(buffer, 0, len);
        }
        byte[] bytes = os.toByteArray();
        String wxNotifyString = new String(bytes, "UTF-8");
        //转换为map
        Map<String, String> map = WXPayUtil.xmlToMap(wxNotifyString);
        //发送支付结果
        String attachString = map.get("attach");
        Map<String,String> attach = JSONObject.parseObject(attachString, Map.class);
        rabbitTemplate.convertAndSend(attach.get("exchange"),attach.get("routingKey"), JSONObject.toJSONString(map));
        //返回微信,告知收到了结果
        Map<String,String> returnMap = new HashMap<>();
        returnMap.put("return_code","SUCCESS");
        returnMap.put("return_msg","OK");
        return WXPayUtil.mapToXml(returnMap);
    }

}
