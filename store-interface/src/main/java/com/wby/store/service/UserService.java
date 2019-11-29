package com.wby.store.service;

import com.wby.store.bean.UserInfo;

import java.util.List;

public interface UserService {
    /**
     * 查询所有数据
     * @return
     */
    List<UserInfo> findAll();

    /**
     *
     * @param userInfo
     */
    void addUser(UserInfo userInfo);

    /**
     * 修改数据
     */
    void updUser(UserInfo userInfo);
    /**
     * 修改数据
     */
    void updUserByName(UserInfo userInfo);

    // 删除 boolean , int ,void

    /**
     *  删除数据
     */
    void delUser(UserInfo userInfo);
}
