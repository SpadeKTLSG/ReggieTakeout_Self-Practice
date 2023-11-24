package com.tlsg.takeout.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tlsg.takeout.entity.Orders;


public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     */
    void submit(Orders orders);
}
