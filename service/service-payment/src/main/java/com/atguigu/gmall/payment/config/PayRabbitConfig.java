package com.atguigu.gmall.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付结果通知的消息队列
 */
@Configuration
public class PayRabbitConfig {

    /**
     * 支付服务的交换机
     * @return
     */
    @Bean("payExchange")
    public Exchange payExchange(){
        return ExchangeBuilder.directExchange("pay_exchange").build();
    }

    /**
     * 微信支付渠道的队列
     * @return
     */
    @Bean("wxPayQueue")
    public Queue wxPayQueue(){
        return QueueBuilder.durable("wx_pay_queue").build();
    }

    /**
     * 支付宝支付渠道的队列
     * @return
     */
    @Bean("aliPayQueue")
    public Queue aliPayQueue(){
        return QueueBuilder.durable("ali_pay_queue").build();
    }

    /**
     * 微信支付的队列绑定
     * @param payExchange
     * @param wxPayQueue
     * @return
     */
    @Bean
    public Binding wxBinding(@Qualifier("payExchange") Exchange payExchange,
                             @Qualifier("wxPayQueue") Queue wxPayQueue){
        return BindingBuilder.bind(wxPayQueue).to(payExchange).with("pay.wx").noargs();
    }

    /**
     * 支付宝支付的队列绑定
     * @param payExchange
     * @param aliPayQueue
     * @return
     */
    @Bean
    public Binding aliBinding(@Qualifier("payExchange") Exchange payExchange,
                             @Qualifier("aliPayQueue") Queue aliPayQueue){
        return BindingBuilder.bind(aliPayQueue).to(payExchange).with("pay.ali").noargs();
    }
}
