package com.atguigu.gmall.product.service;

import com.alibaba.fastjson2.JSONObject;

import java.util.List;

/**
 * 首页数据展示使用的相关接口
 */
public interface IndexService {

    /**
     * 查询首页的分类列表
     * @return
     */
    public List<JSONObject> getIndexCategory();
}
