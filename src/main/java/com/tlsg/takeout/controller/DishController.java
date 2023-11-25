package com.tlsg.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tlsg.takeout.common.CustomException;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private RedisTemplate redisTemplate; //加入菜品缓存


    //新增菜品信息
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        //粗暴: 清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //精细: 清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

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

    //修改菜品
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);


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

    //大佬制作的通用删除
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("删除的ids：{}", ids);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);
        int count = (int) dishService.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("删除列表中存在启售状态商品，无法删除");
        }
        dishService.removeByIds(ids);
        return R.success("删除成功");
    }

    
    //条件查询对应的菜品数据, 注意停止售卖的菜品不应该被查询出来
    //扩充了前台的功能，根据菜品分类id查询对应的菜品数据
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        //查询前采用缓存功能, 如果缓存中有数据, 直接返回缓存中的数据

        List<DishDto> dishDtoList = null;

        //动态构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();//dish_1397844391040167938_1

        //先尝试从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null) {            //如果存在，直接返回，无需查询数据库
            return R.success(dishDtoList);
        }

        //如果不存在，查询数据库，然后将查询到的数据存入redis中

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus, 1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item) -> {
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

        //如果是从数据库查的, 要将查询到的菜品数据缓存到Redis以便下一次查询
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);


        return R.success(dishDtoList);
    }

    //菜品起售停售
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status, Long ids) {
        log.info("status:{},ids:{}", status, ids);
        Dish dish = dishService.getById(ids);
        if (dish != null) {
            //直接用它传进来的这个status改就行
            dish.setStatus(status);
            dishService.updateById(dish);
            return R.success("售卖状态修改成功");
        }
        return R.error("系统繁忙，请稍后再试");
    }

    //菜品批量启售/停售
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status, @RequestParam List<Long> ids) {
        log.info("status:{},ids:{}", status, ids);
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(ids != null, Dish::getId, ids);
        updateWrapper.set(Dish::getStatus, status);
        dishService.update(updateWrapper);
        return R.success("批量操作成功");
    }

}