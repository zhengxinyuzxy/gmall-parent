package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;

import java.util.List;

public interface UserService {

    /**
     * 根据用户的登录名查询用户的基本信息
     * @param username
     * @return
     */
    public UserInfo getUserInfoByLoginName(String username);

    /**
     * 获取用户的收货地址列表
     * @param username
     * @return
     */
    public List<UserAddress> getUserAddress(String username);
}
