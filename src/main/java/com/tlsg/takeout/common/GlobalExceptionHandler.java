package com.tlsg.takeout.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
//指定拦截的注解类型: RestController和Controller
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    //SQLIntegrityConstraintViolationException, 就是数据库的完整性约束异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage()); //日志打印错误信息

        if (ex.getMessage().contains("Duplicate entry")) { //如果错误信息包含Duplicate entry, 则说明是主键重复了
            String[] split = ex.getMessage().split(" "); //将错误信息按照空格分割, 返回一个字符串数组
            String msg = split[2] + "已存在,请尝试其他名称"; //获取第三个元素, 即重复的主键值对象
            return R.error(msg);
        }

        return R.error("未知错误, 请联系管理员");
    }

    //自定义业务异常
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }
}
