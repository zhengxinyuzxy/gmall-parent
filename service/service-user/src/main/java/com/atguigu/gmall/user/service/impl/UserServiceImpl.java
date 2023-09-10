package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserInfoMapper userInfoMapper;
    /**
     * 根据用户的登录名查询用户的基本信息
     *
     * @param username
     * @return
     */
    @Override
    public UserInfo getUserInfoByLoginName(String username) {
        return userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getLoginName, username));
    }

    @Resource
    private UserAddressMapper userAddressMapper;
    /**
     * 获取用户的收货地址列表
     *
     * @param username
     * @return
     */
    @Override
    public List<UserAddress> getUserAddress(String username) {
        return userAddressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, username));
    }
}
