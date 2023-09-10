package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchResponseAttrVo;
import com.atguigu.gmall.model.list.SearchResponseTmVo;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 商品搜索
     *
     * @param searchData
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchData) {
        //参数校验
        if(searchData == null){
            return null;
        }
        //拼接查询条件
        SearchRequest searchRequest = builderQueryParam(searchData);
        try {
            //执行查询获取结果
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //解析结果返回数据
            Map<String, Object> searchResult = getSearchResult(search);
            searchResult.put("page", getPage(searchData.get("page")));
            return searchResult;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 构建查询条件
     * @param searchData
     * @return
     */
    private SearchRequest builderQueryParam(Map<String, String> searchData) {
        //初始化
        SearchRequest searchRequest = new SearchRequest("goods_java0107");
        //条件构造器初始化
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //构建bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //关键字查询
        String keywords = searchData.get("keywords");
        if(!StringUtils.isEmpty(keywords)){
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", keywords));
        }
        //品牌的查询: 165:美的
        String tradeMark = searchData.get("tradeMark");
        if(!StringUtils.isEmpty(tradeMark)){
            //获取id或者获取名字
            String[] split = tradeMark.split(":");
            //方案一,根据品牌的id查询
            boolQueryBuilder.must(QueryBuilders.termQuery("tmId", split[0]));
            //方案二,根据品牌的名字查询
//            boolQueryBuilder.must(QueryBuilders.termQuery("tmName", split[1]));
        }
        //平台属性查询
        for (Map.Entry<String, String> entry : searchData.entrySet()) {
            //所有参数的名字
            String key = entry.getKey();
            //判断是否为平台属性 attr_颜色=23:红色
            if(key.startsWith("attr_")){
                //切割获取平台属性的id和用户选的值
                String[] split = entry.getValue().split(":");
                BoolQueryBuilder nestedBoolQueryBuilder = QueryBuilders.boolQuery();
                nestedBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                nestedBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));
                boolQueryBuilder.must(QueryBuilders.nestedQuery("attrs", nestedBoolQueryBuilder,ScoreMode.None));
            }
        }
        //价格查询price=500-1000元    3000元以上
        String price = searchData.get("price");
        if(!StringUtils.isEmpty(price)){
            //数据处理--->500-1000/3000
            price = price.replace("元", "").replace("以上", "");
            String[] split = price.split("-");
            boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gte(split[0]));
            //判断是否有第二个值
            if(split.length > 1){
                boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lt(split[1]));
            }
        }
        //设置查询条件
        builder.query(boolQueryBuilder);
        //设置品牌的聚合查询条件
        builder.aggregation(
                AggregationBuilders.terms("aggTmId").field("tmId")
                    .subAggregation(AggregationBuilders.terms("aggTmName").field("tmName"))
                    .subAggregation(AggregationBuilders.terms("aggTmLogoUrl").field("tmLogoUrl"))
                    .size(10000000)
        );
        //设置平台属性的聚合查询条件
        builder.aggregation(
                AggregationBuilders.nested("aggAttrs", "attrs")
                    .subAggregation(
                            AggregationBuilders.terms("aggAttrId").field("attrs.attrId")
                                    .subAggregation(AggregationBuilders.terms("aggAttrName").field("attrs.attrName"))
                                    .subAggregation(AggregationBuilders.terms("aggAttrValue").field("attrs.attrValue"))
                                    .size(10000000)
                    )
        );
        //分页和排序
        builder.size(100);
        //页码1-->0 2>100 3>200
        Integer page = getPage(searchData.get("page"));
        builder.from((page - 1) * 100);
        //排序的实现
        String softField = searchData.get("softField");
        if(!StringUtils.isEmpty(softField)){
            String softRule = searchData.get("softRule");
            if(!StringUtils.isEmpty(softRule)){
                builder.sort(softField, SortOrder.valueOf(softRule));
            }else{
                builder.sort(softField, SortOrder.DESC);
            }
        }
        //设置高亮查询
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<font style='color:red'>");
        highlightBuilder.postTags("</font>");
        builder.highlighter(highlightBuilder);
        //设置条件
        searchRequest.source(builder);
        //返回结果
        return searchRequest;
    }

    /**
     * 计算页码
     * @param page
     * @return
     */
    private Integer getPage(String page) {
        try {
            //防止用户传递页码为字符串,负数呢?
            return Integer.parseInt(page)>0?Integer.parseInt(page):1;
        }catch (Exception e){
            return 1;
        }
    }

    /**
     * 解析搜索到的结果
     * @param search
     */
    private Map<String, Object> getSearchResult(SearchResponse search) {
        //返回结果初始化
        Map<String, Object> result = new HashMap<>();
        //获取命中的数据
        SearchHits hits = search.getHits();
        //总数量
        long totalHits = hits.getTotalHits();
        result.put("total", totalHits);
        result.put("size", 100);
        //获取迭代器
        Iterator<SearchHit> iterator = hits.iterator();
        //商品列表初始化
        List<Goods> goodsList = new ArrayList<>();
        //循环解析命中的商品数据
        while (iterator.hasNext()){
            SearchHit next = iterator.next();
            //获取json类型的字符串数据
            String sourceAsString = next.getSourceAsString();
            //反序列化
            Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
            //获取高亮的数据
            HighlightField highlightField = next.getHighlightFields().get("title");
            if(highlightField != null){
                Text[] fragments = highlightField.getFragments();
                if(fragments != null && fragments.length > 0){
                    String title = "";
                    for (Text fragment : fragments) {
                        title += fragment;
                    }
                    goods.setTitle(title);
                }
            }
            goodsList.add(goods);
        }
        //存储商品列表的数据
        result.put("goodsList", goodsList);
        //获取所有的聚合结果
        Map<String, Aggregation> aggregationMap = search.getAggregations().asMap();
        //获取品牌的聚合结果
        List<SearchResponseTmVo> tradeMarkList = getTradeMarkList(aggregationMap);
        result.put("tradeMarkList", tradeMarkList);
        //获取平台属性的聚合结果
        List<SearchResponseAttrVo> baseAttrInfoList = getBaseAttrInfoList(aggregationMap);
        result.put("baseAttrInfoList", baseAttrInfoList);
        //返回
        return result;
    }

    /**
     * 获取平台属性的聚合结果
     * @param aggregationMap
     */
    private List<SearchResponseAttrVo> getBaseAttrInfoList(Map<String, Aggregation> aggregationMap) {
        //返回结果初始化
        List<SearchResponseAttrVo> searchResponseAttrVos = new ArrayList<>();
        //获取所有平台属性nested对象的聚合结果
        ParsedNested aggAttrs = (ParsedNested)aggregationMap.get("aggAttrs");
        //获取遍历结果
        ParsedLongTerms aggAttrIds = aggAttrs.getAggregations().get("aggAttrId");
        for (Terms.Bucket bucket : aggAttrIds.getBuckets()) {
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //获取平台属性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            searchResponseAttrVo.setAttrId(attrId);
            //获取平台属性的名字的聚合结果
            ParsedStringTerms aggAttrName = bucket.getAggregations().get("aggAttrName");
            List<? extends Terms.Bucket> buckets = aggAttrName.getBuckets();
            if(!buckets.isEmpty()){
                String attrName = buckets.get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);
            }
            //获取平台属性值的聚合结果
            ParsedStringTerms aggAttrValue = bucket.getAggregations().get("aggAttrValue");
            //平台属性值的列表初始化
            List<String> attrValus = new ArrayList<>();
            for (Terms.Bucket aggAttrValueBucket : aggAttrValue.getBuckets()) {
                String keyAsString = aggAttrValueBucket.getKeyAsString();
                attrValus.add(keyAsString);
            }
            searchResponseAttrVo.setAttrValueList(attrValus);
            searchResponseAttrVos.add(searchResponseAttrVo);
        }
        return searchResponseAttrVos;
    }

    /**
     * 获取品牌的聚合结果
     * @param aggregationMap
     */
    private List<SearchResponseTmVo> getTradeMarkList(Map<String, Aggregation> aggregationMap) {
        //返回结果初始化
        List<SearchResponseTmVo> searchResponseTmVos = new ArrayList<>();
        //获取品牌的聚合结果
        ParsedLongTerms aggTmId = (ParsedLongTerms)aggregationMap.get("aggTmId");
        //遍历结果
        for (Terms.Bucket bucket : aggTmId.getBuckets()) {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //获取品牌的id
            long tmId = bucket.getKeyAsNumber().longValue();
            searchResponseTmVo.setTmId(tmId);
            //获取品牌的名字
            ParsedStringTerms aggTmName = bucket.getAggregations().get("aggTmName");
            List<? extends Terms.Bucket> tmNameBuckets = aggTmName.getBuckets();
            if(!tmNameBuckets.isEmpty()){
                String tmName = aggTmName.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmName(tmName);
            }
            //获取品牌的logourl
            ParsedStringTerms aggTmLogoUrl = bucket.getAggregations().get("aggTmLogoUrl");
            List<? extends Terms.Bucket> tmLogoBuckets = aggTmLogoUrl.getBuckets();
            if(!tmLogoBuckets.isEmpty()){
                String tmLogoUrl = aggTmLogoUrl.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            }
            searchResponseTmVos.add(searchResponseTmVo);
        }
        //返回结果
        return searchResponseTmVos;
    }
}