package com.atguigu.gmall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 超时订单相关的队列和交换机的创建配置
 */
@Configuration
public class OrderRabbitConfig {

    /**
     * 正常接收延迟消息的交换机
     * @return
     */
    @Bean("orderExchange")
    public Exchange orderExchange(){
        return ExchangeBuilder.directExchange("order_exchange").build();
    }

    /**
     * 死信队列,没有消费者
     * @return
     */
    @Bean("delayQueue")
    public Queue delayQueue(){
        return QueueBuilder
                .durable("delay_queue")
                .withArgument("x-dead-letter-exchange","delay_exchange") //转发交换机
                .withArgument("x-dead-letter-routing-key","order.cancle") // 转发的规则
                .build();
    }

    /**
     * 死信队列与正常交换机的绑定关系
     * @param orderExchange
     * @param delayQueue
     * @return
     */
    @Bean
    public Binding delayBinding(@Qualifier("orderExchange") Exchange orderExchange,
                                @Qualifier("delayQueue") Queue delayQueue){
        return BindingBuilder.bind(delayQueue).to(orderExchange).with("order.delay").noargs();
    }

    /**
     * 接收死信消息的交换机
     * @return
     */
    @Bean("delayExchange")
    public Exchange delayExchange(){
        return ExchangeBuilder.directExchange("delay_exchange").build();
    }
    /**
     * 接收死信的正常队列,有消费者
     * @return
     */
    @Bean("orderQueue")
    public Queue orderQueue(){
        return QueueBuilder.durable("order_queue").build();
    }

    /**
     * 死信交换机和正常队列的绑定关系
     * @param delayExchange
     * @param orderQueue
     * @return
     */
    @Bean
    public Binding orderBinding(@Qualifier("delayExchange") Exchange delayExchange,
                                @Qualifier("orderQueue") Queue orderQueue){
        return BindingBuilder.bind(orderQueue).to(delayExchange).with("order.cancle").noargs();
    }
}
