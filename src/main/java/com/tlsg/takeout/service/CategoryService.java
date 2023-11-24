package com.tlsg.takeout.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tlsg.takeout.entity.Category;
import org.springframework.stereotype.Service;

@Service
public interface CategoryService extends IService<Category> {
    void remove(Long id);
}
