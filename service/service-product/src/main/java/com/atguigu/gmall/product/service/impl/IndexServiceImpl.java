package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;
    /**
     * 查询首页的分类列表
     *
     * @return
     */
    @Override
    public List<JSONObject> getIndexCategory() {
        //#找出所有的一级分类List<BaseCategoryView>
        List<BaseCategoryView> categoryViewList = baseCategoryViewMapper.selectList(null);
        //#基于这个list赛选出所有的一级分类进行一次基于category1_id的分组GROUP BY
        Map<Long, List<BaseCategoryView>> category1Map =
                categoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        //针对每个一级分类进行遍历
        List<JSONObject> category1JsonList = new ArrayList<>();
        for (Map.Entry<Long, List<BaseCategoryView>> category1 : category1Map.entrySet()) {
            JSONObject jsonObject1 = new JSONObject();
            //一级分类的id
            Long category1Key = category1.getKey();
            jsonObject1.put("categoryId", category1Key);
            //这个一级分类对应的所有的二级和三级分类
            List<BaseCategoryView> categoryViewList2 = category1.getValue();
            jsonObject1.put("categoryName", categoryViewList2.get(0).getCategory1Name());
            //对二级分类进行分组
            Map<Long, List<BaseCategoryView>> category2Map =
                    categoryViewList2.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            //遍历
            List<JSONObject> category2JsonList = new ArrayList<>();
            for (Map.Entry<Long, List<BaseCategoryView>> category2 : category2Map.entrySet()) {
                JSONObject jsonObject2 = new JSONObject();
                //二级分类的id
                Long category2Key = category2.getKey();
                //这个二级分类对应的所有的三级分类
                List<BaseCategoryView> categoryViewList3 = category2.getValue();
                //将数据转换为json对象
                List<JSONObject> category3JsonList = categoryViewList3.stream().map(category3 -> {
                    //声明对象
                    JSONObject jsonObject3 = new JSONObject();
                    //设置三级分类的id
                    jsonObject3.put("categoryId", category3.getCategory3Id());
                    //设置三级分类的name
                    jsonObject3.put("categoryName", category3.getCategory3Name());
                    //返回结果
                    return jsonObject3;
                }).collect(Collectors.toList());
                //设置三级分类的id
                jsonObject2.put("categoryId", category2Key);
                //设置三级分类的name
                jsonObject2.put("categoryName", categoryViewList3.get(0).getCategory2Name());
                //子分类
                jsonObject2.put("categoryChild", category3JsonList);
                //保存数据
                category2JsonList.add(jsonObject2);
            }
            jsonObject1.put("categoryChild", category2JsonList);
            //保存数据
            category1JsonList.add(jsonObject1);
        }
        return category1JsonList;
    }
}
