package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test/product/ttt")
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping
    public void test(){
        testService.setValueRedission();
    }
}
