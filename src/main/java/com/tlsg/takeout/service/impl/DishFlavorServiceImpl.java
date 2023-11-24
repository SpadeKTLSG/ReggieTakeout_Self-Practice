package com.tlsg.takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.tlsg.takeout.entity.DishFlavor;
import com.tlsg.takeout.mapper.DishFlavorMapper;
import com.tlsg.takeout.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
