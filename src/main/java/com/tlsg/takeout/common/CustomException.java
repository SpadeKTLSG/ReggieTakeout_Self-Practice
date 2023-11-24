package com.tlsg.takeout.common;

/**
 * 自定义业务异常类
 */
public class CustomException extends RuntimeException { //继承运行时异常
    public CustomException(String message) { //返回消息
        super(message);
    }
    //同时需要在异常处理类中添加异常处理方法
}
