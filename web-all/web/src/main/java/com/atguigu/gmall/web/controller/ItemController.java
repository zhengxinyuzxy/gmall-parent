package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.item.feign.ItemFeign;
import com.atguigu.gmall.list.feign.ListFeign;
import com.atguigu.gmall.web.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

@Controller
@RequestMapping(value = "/page/item")
public class ItemController {

    @Autowired
    private ItemFeign itemFeign;

    /**
     * 商品详情页打开
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping
    public String itemIndex(Long skuId, Model model){
        //远程调用查询商品详情的所有数据
        Map<String, Object> skuItem = itemFeign.getSkuItem(skuId);
        //将数据存入model供页面展示
        model.addAllAttributes(skuItem);
        //返回页面
        return "item";
    }

    @Autowired
    private TemplateEngine templateEngine;
    /**
     * 创建商品详情页的静态页面
     * @param skuId
     * @return
     */
    @GetMapping(value = "/createItemHtml")
    @ResponseBody
    public String createItemHtml(Long skuId) throws Exception{
        //远程调用查询商品详情的所有数据
        Map<String, Object> skuItem = itemFeign.getSkuItem(skuId);

        //创建文件
        File file = new File("D:/",skuId + ".html");
        //定义输出流
        PrintWriter writer = new PrintWriter(file);
        //创建上下文
        Context context = new Context();
        context.setVariables(skuItem);
        //基于模板生成静态页面
        /**
         * 1.模板的名字
         * 2.上下文
         * 3.输出流
         */
        templateEngine.process("item", context, writer);
        writer.close();

        return "success";
    }

    @Autowired
    private ListFeign listFeign;
    /**
     * 商品搜索页面
     * @return
     */
    @GetMapping(value = "/list")
    public String list(@RequestParam Map<String, String> searchData, Model model){
        //远程调用list微服务,获取匹配的数据
        Map<String, Object> search = listFeign.search(searchData);
        //将数据写入model中区
        model.addAllAttributes(search);
        //再将查询的条件也放入model中区
        model.addAttribute("searchData", searchData);
        //获取url'
        String url = getUrl(searchData);
        model.addAttribute("url", url);
        System.out.println(url);
        //获取分页的信息
        Object page = search.get("page");
        Object size = search.get("size");
        Object total = search.get("total");
        Page<Object> pageInfo = new Page<Object>(
                Long.parseLong(total.toString()),
                Integer.parseInt(page.toString()),
                Integer.parseInt(size.toString()));
        model.addAttribute("pageInfo", pageInfo);
        pageInfo.getNext();
        pageInfo.getUpper();
        //打开页面
        return "list";
    }

    /**
     * 获取当前的url
     * @param searchData
     * @return
     */
    private String getUrl(Map<String, String> searchData){
        String url = "/page/item/list?";
        //对所有的查询条件进行遍历,构建url
        for (Map.Entry<String, String> entry : searchData.entrySet()) {
            String key = entry.getKey();
            if(!key.equals("page") && !key.equals("softField") && !key.equals("softRule")){
                String value = entry.getValue();
                url = url + key + "=" + value + "&";
            }
        }
        return url.substring(0, url.length() - 1);
    }

}
