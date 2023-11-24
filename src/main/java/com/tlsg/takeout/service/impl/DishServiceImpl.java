package com.tlsg.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.tlsg.takeout.dto.DishDto;
import com.tlsg.takeout.entity.Dish;
import com.tlsg.takeout.entity.DishFlavor;
import com.tlsg.takeout.mapper.DishMapper;
import com.tlsg.takeout.service.DishFlavorService;
import com.tlsg.takeout.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();//菜品id

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        //peek相对于map，不需要写return，只是对流中的元素进行操作，不会改变流中的元素
        flavors = flavors.stream().peek((item) -> { // map -> peek, 逻辑: 将flavors集合中的每一个元素item，转换为DishFlavor对象;
            item.setDishId(dishId);
        }).collect(Collectors.toList());

        //保存口味数据到表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    //ID查询对应菜品信息
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto); //使用BeanUtils工具类，将dish的属性拷贝到dishDto中

        //查看当前菜品对应口味信息,封装到dishDto中
        List<DishFlavor> flavors = dishFlavorService.list(new LambdaQueryWrapper<>(new DishFlavor()).eq(DishFlavor::getDishId, id)); //简单写法
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    //更新菜品信息
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) { //dishDto是前端传过来的数据
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        //删除当前菜品对应的口味数据
        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().peek((item) -> { //逻辑: 将flavors集合中的每一个元素item，转换为DishFlavor对象
            item.setDishId(dishDto.getId());
        }).toList(); //将流转换为List集合, 也可.collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }
}
