package com.atguigu.gmall.pay.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-payment", path = "/api/ali/pay")
public interface AliPayFeign {

    /**
     * 关闭交易
     * @param orderId
     * @return
     */
    @GetMapping(value = "/closePay/{orderId}")
    public String closePay(@PathVariable(value = "orderId") Long orderId);

}
