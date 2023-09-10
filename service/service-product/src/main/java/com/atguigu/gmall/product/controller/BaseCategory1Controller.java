package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.product.service.BaseCategory1Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 一级分类的控制层
 */
@RestController
@RequestMapping(value = "/api/category1")
public class BaseCategory1Controller {

    @Autowired
    private BaseCategory1Service baseCategory1Service;

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping(value = "/getById/{id}")
    public Result getById(@PathVariable(value = "id") Long id){
        return Result.ok(baseCategory1Service.getById(id));
    }

    /**
     * 查询全部的数据
     * @return
     */
    @GetMapping(value = "/getAll")
    public Result getAll(){
        return Result.ok(baseCategory1Service.list(null));
    }

    /**
     * 新增
     * @param baseCategory1
     * @return
     */
    @PostMapping
    public Result add(@RequestBody BaseCategory1 baseCategory1){
        return Result.ok(baseCategory1Service.save(baseCategory1));
    }

    /**
     * 修改
     * @param baseCategory1
     * @return
     */
    @PutMapping
    public Result update(@RequestBody BaseCategory1 baseCategory1){
        return Result.ok(baseCategory1Service.updateById(baseCategory1));
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable(value = "id") Long id){
        return Result.ok(baseCategory1Service.removeById(id));
    }

    /**
     * 条件查询
     * @param baseCategory1
     * @return
     */
    @PostMapping(value = "/search")
    public Result search(@RequestBody BaseCategory1 baseCategory1){
        return Result.ok(baseCategory1Service.search(baseCategory1));
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/getPage/{page}/{size}")
    public Result getPage(@PathVariable(value = "page") Integer page,
                          @PathVariable(value = "size") Integer size){
        return Result.ok(baseCategory1Service.pageGet(page, size));
    }

    /**
     * 分页条件查询
     * @param baseCategory1
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    public Result search(@RequestBody BaseCategory1 baseCategory1,
                         @PathVariable(value = "page") Integer page,
                         @PathVariable(value = "size") Integer size){
        return Result.ok(baseCategory1Service.search(baseCategory1, page, size));
    }
}
