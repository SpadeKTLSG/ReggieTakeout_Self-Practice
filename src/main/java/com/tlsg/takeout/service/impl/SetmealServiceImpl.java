package com.tlsg.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tlsg.takeout.common.CustomException;
import com.tlsg.takeout.dto.SetmealDto;
import com.tlsg.takeout.entity.Setmeal;
import com.tlsg.takeout.entity.SetmealDish;
import com.tlsg.takeout.mapper.SetmealMapper;
import com.tlsg.takeout.service.SetmealDishService;
import com.tlsg.takeout.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    // 新增套餐，同时需要保存套餐和菜品的关联关系
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);


        //保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes(); // 获取套餐中的菜品列表

        setmealDishes.stream().peek((item) -> { // 遍历菜品列表, 为每个菜品设置套餐id
            item.setSetmealId(setmealDto.getId());
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes); // 批量保存菜品列表
    }


    //删除套餐，同时需要删除套餐和菜品的关联数据
    //要求: 状态在停售的套餐才能删除, 如果套餐已经上架, 则不能删除, 需要先下架
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {

        //流程:  //查询套餐状态，确定是否可用删除;  //如果不能删除，抛出一个业务异常;  
        // 如果可以删除，先删除套餐表中的数据---setmeal;
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询套餐状态，确定是否可用删除

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);

        int count = (int) this.count(queryWrapper);
        if (count > 0) {
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);

        //删除关系表中的数据----setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);
    }

}
