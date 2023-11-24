package com.tlsg.takeout.common;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//元数据对象处理器
@Component
@Slf4j
public class MyMetaObjecthandler implements MetaObjectHandler {

    //插入操作, 自动填充
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]...");
        log.info(metaObject.toString()); // 将metaObject对象转换为字符串打印到日志中

        //简单值处理: 直接按照属性名和属性值进行填充
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());

        //复杂值处理: 需要先获取当前登录用户的Session对象, 然后再进行进一步处理
        //BaseContext
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());

    }

    //更新操作, 自动填充
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]...");
        log.info("metaObject对象: " + metaObject.toString()); // 将metaObject对象转换为字符串打印到日志中


        long id = Thread.currentThread().getId();
        log.info("线程id为：{}", id);

        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
