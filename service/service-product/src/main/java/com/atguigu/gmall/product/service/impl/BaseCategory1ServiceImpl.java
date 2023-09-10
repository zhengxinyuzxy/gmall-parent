package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.product.mapper.BaseCategory1Mapper;
import com.atguigu.gmall.product.service.BaseCategory1Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class BaseCategory1ServiceImpl
        extends ServiceImpl<BaseCategory1Mapper, BaseCategory1>
        implements BaseCategory1Service {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    /**
     * 条件查询
     *
     * @param baseCategory1
     * @return
     */
    @Override
    public List<BaseCategory1> search(BaseCategory1 baseCategory1) {
        //参数校验
        if(baseCategory1 == null){
            //若没有任何条件,则查询所有的数据返回
            return baseCategory1Mapper.selectList(null) ;
        }
        //拼接条件
        LambdaQueryWrapper wrapper = getQueryWrapper(baseCategory1);
        //执行查询获取结果
        List<BaseCategory1> list = baseCategory1Mapper.selectList(wrapper);
        //返回
        return list;
    }

    /**
     * 分页查询
     *
     * @param page : 页码
     * @param size : 每页显示数量
     * @return
     */
    @Override
    public IPage<BaseCategory1> pageGet(Integer page, Integer size) {
        //参数校验
        if(page == null || size == null){
            throw new RuntimeException("参数错误!");
        }
        //构建分页信息,执行查询获取结果
        IPage<BaseCategory1> baseCategory1IPage = baseCategory1Mapper.selectPage(new Page<>(page, size), null);
        //返回
        return baseCategory1IPage;
    }

    /**
     * 条件查询
     *
     * @param baseCategory1
     * @param page          : 页码
     * @param size          : 每页显示数量
     * @return
     */
    @Override
    public IPage<BaseCategory1> search(BaseCategory1 baseCategory1,
                                      Integer page,
                                      Integer size) {
        //参数校验
        if(page == null || size == null || baseCategory1 == null){
            throw new RuntimeException("参数错误!");
        }
        //拼接条件
        LambdaQueryWrapper wrapper = getQueryWrapper(baseCategory1);
        //构建分页信息,执行查询获取结果
        IPage<BaseCategory1> baseCategory1IPage =
                baseCategory1Mapper.selectPage(new Page<>(page, size), wrapper);
        //返回结果
        return baseCategory1IPage;
    }

    /**
     * 拼接条件
     * @return
     */
    private LambdaQueryWrapper getQueryWrapper(BaseCategory1 baseCategory1){
        //声明条件构造器
        LambdaQueryWrapper<BaseCategory1> wrapper = new LambdaQueryWrapper<>();
        //拼接条件:判断id是否为空
        if(baseCategory1.getId() != null){
            wrapper.eq(BaseCategory1::getId, baseCategory1.getId());
        }
        //判断name是否为空
        if(!StringUtils.isEmpty(baseCategory1.getName())){
            wrapper.like(BaseCategory1::getName, baseCategory1.getName());
        }
        return wrapper;
    }
}
