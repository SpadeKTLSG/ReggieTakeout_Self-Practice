package com.tlsg.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tlsg.takeout.common.R;
import com.tlsg.takeout.dto.DishDto;
import com.tlsg.takeout.entity.Category;
import com.tlsg.takeout.entity.Dish;
import com.tlsg.takeout.entity.DishFlavor;
import com.tlsg.takeout.service.CategoryService;
import com.tlsg.takeout.service.DishFlavorService;
import com.tlsg.takeout.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

//菜品管理
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    //新增菜品信息
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    //分页查询菜品信息
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器对象
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>(new Dish())
                .like(name != null, Dish::getName, name) //过滤
                .orderByDesc(Dish::getUpdateTime); //排序

        dishService.page(pageInfo, queryWrapper); //分页查询

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records"); //将pageInfo中的属性拷贝到dishDtoPage中, 但是不拷贝records属性

        //查询菜品分类名称
        List<Dish> records = pageInfo.getRecords();


        List<DishDto> list = records.stream().map((item) -> { //这里使用stream流的map方法，将records集合中的每一个元素item，转换为DishDto对象
            DishDto dishDto = new DishDto(); //创建一个相比于原来Dish对象扩充后的DishDto对象, 用来接受转换后的数据

            BeanUtils.copyProperties(item, dishDto);//copyProperties方法，将item中的属性拷贝到dishDto中

            //这还不够, 还需要分类名称, 需要查询菜品分类表category
            Long categoryId = item.getCategoryId(); //菜品分类id

            Category category = categoryService.getById(categoryId); //找分类对象
            if (category != null) {
                String categeryName = category.getName();
                dishDto.setCategoryName(categeryName); //设置分类名称
            }
            return dishDto; //内嵌方法返回dishDto对象
        }).toList(); //将流转换为List集合, 也可.collect(Collectors.toList()); 


        dishDtoPage.setRecords(list); //将list集合设置到dishDtoPage对象中

        return R.success(dishDtoPage);
    }

    //id查询菜品信息和口味信息 
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    //更新菜品信息
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        return R.success("更新菜品成功");
    }

    //删除菜品信息(自制)
    @DeleteMapping
    public R<String> delete(Long ids) {
        if (ids == null)
            return R.error("删除菜品失败，id不能为空");

        log.info("删除菜品，id为：{}", ids);
        dishService.removeById(ids);

        return R.success("菜品信息删除成功");
    }

    //条件查询对应的菜品数据, 注意停止售卖的菜品不应该被查询出来
    //扩充了前台的功能，根据菜品分类id查询对应的菜品数据
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus, 1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }


}