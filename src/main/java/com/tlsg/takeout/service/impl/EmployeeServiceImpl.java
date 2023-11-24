package com.tlsg.takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tlsg.takeout.entity.Employee;
import com.tlsg.takeout.mapper.EmployeeMapper;
import com.tlsg.takeout.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    //这里继承了ServiceImpl, 实现了IService接口, 所以不需要再写方法了
}
