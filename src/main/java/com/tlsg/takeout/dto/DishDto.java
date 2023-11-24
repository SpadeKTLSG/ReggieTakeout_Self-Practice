package com.tlsg.takeout.dto;


import com.tlsg.takeout.entity.Dish;
import com.tlsg.takeout.entity.DishFlavor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true) //EqualsAndHashCode代表
@Data
public class DishDto extends Dish { //date transfer object
    //菜品对应的口味数据
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
