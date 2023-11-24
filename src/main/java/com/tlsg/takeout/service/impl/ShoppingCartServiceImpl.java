package com.tlsg.takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tlsg.takeout.entity.ShoppingCart;
import com.tlsg.takeout.mapper.ShoppingCartMapper;
import com.tlsg.takeout.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

}
