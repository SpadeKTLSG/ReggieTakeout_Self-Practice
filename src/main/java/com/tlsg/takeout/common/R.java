package com.tlsg.takeout.common;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用结果类:R
 * 返回结果的封装
 *
 * @param <T>
 */
@Data
public class R<T> {

    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg; //错误信息

    private T data; //数据

    private Map<Object, Object> map = new HashMap<>(); //动态数据

    public static <T> R<T> success(T object) { //响应成功
        R<T> r = new R<>();
        r.data = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> error(String msg) { //响应失败
        R<T> r = new R<>();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) { //动态添加数据
        this.map.put(key, value);
        return this;
    }

}
