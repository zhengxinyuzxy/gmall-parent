package com.atguigu.gmall.activity.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("seckill_order")
public class SeckillOrder implements Serializable{

   @TableField("id")
   private String id;//主键

   @TableField("goods_id")
   private String goodsId;//秒杀商品ID

   @TableField("num")
   private Integer num;

   @TableField("money")
   private String money;//支付金额

   @TableField("user_id")
   private String userId;//用户

   @TableField("create_time")
   private Date createTime;//创建时间

   @TableField("pay_time")
   private Date payTime;//支付时间

   @TableField("status")
   private String status;//状态，0未支付，1已支付,2支付失败,3.订单主动取消 4.订单超时未支付

   @TableField("address")
   private String address;//收货人地址

   @TableField("mobile")
   private String mobile;//收货人电话

   @TableField("receiver")
   private String receiver;//收货人

   @TableField("out_trade_no")
   private String OutTradeNo;//交易流水
}