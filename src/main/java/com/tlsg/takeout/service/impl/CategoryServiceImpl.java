package com.tlsg.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tlsg.takeout.common.CustomException;
import com.tlsg.takeout.entity.Dish;
import com.tlsg.takeout.entity.Setmeal;
import com.tlsg.takeout.mapper.CategoryMapper;
import com.tlsg.takeout.service.CategoryService;
import com.tlsg.takeout.entity.Category;
import com.tlsg.takeout.service.DishService;
import com.tlsg.takeout.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    //需要使用Dish和meal的service, 用于判断是否当前类别与菜品或者套餐是否有关联
    @Autowired
    private DishService dishService;
    //原理相同, 重复即可
    @Autowired
    private SetmealService setmealService;


    //判断无依赖关系后, 根据ID删除否则抛自定义业务异常
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (dishService.count(dishLambdaQueryWrapper.eq(Dish::getCategoryId, id)) > 0)
            throw new CustomException("当前分类下关联了菜品，不能删除");
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (setmealService.count(setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id)) > 0)
            throw new CustomException("当前分类下关联了套餐，不能删除");

        //正常
        super.removeById(id);
    }
}
