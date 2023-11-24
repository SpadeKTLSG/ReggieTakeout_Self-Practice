package com.tlsg.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tlsg.takeout.common.R;
import com.tlsg.takeout.entity.Employee;
import com.tlsg.takeout.service.EmployeeService;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;


@SuppressWarnings("unchecked")
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    //11.22员工登录demo: 前端传入username和password, 后端返回登录成功或失败, JSON格式
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {//还需要一个Request对象来处理Session的内容方便后续登录

        //1.将页面提交的密码password进行md5加密处理, 保存到变量中
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes()); //这里的password.getBytes()是将password转换为byte数组, 然后再进行md5加密处理

        //2.使用MybatisPlus的LambdaQueryWrapper进行查询, 根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>(); //创建一个LambdaQueryWrapper对象
        queryWrapper.eq(Employee::getUsername, employee.getUsername()); //设置查询条件, 这里的Employee::getUsername是一个Lambda表达式, 用于获取Employee对象的username属性
        Employee emp = employeeService.getOne(queryWrapper); //调用getOne方法, 传入LambdaQueryWrapper对象, 返回一个Employee对象


        //3 查到了吗?匹配成功了吗? 两个判断
        if (emp == null) { //如果没有查询到则返回登录失败结果
            return R.error("登录失败, 网络开小差了");
        }

        if (!emp.getPassword().equals(password)) { //密码比对, 如果不一致则返回登录失败结果
            return R.error("密码错误, 登录失败");
        }

        //5 查看状态,如果员工被禁用了, 返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用, 好好反省去吧");
        }

        //登录成功, 将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId()); //将员工id存入Session
        return R.success(emp); //返回登录成功结果

    }
    //http://localhost:8080/employee/login

    //员工登出 - 删:  清理Session中保存的当前登录员工的id
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
    //http://localhost:8080/employee/logout

    //新增员工
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        //获得当前登录用户的id
        Long empId = (Long) request.getSession().getAttribute("employee"); //从Session中获取employee属性

        log.info("正在新增一名员工：{}, ID为 {}", employee.toString(), empId);


        //设置初始密码123456 进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));


        //下面使用MybatisPlus的自动填充功能, 不再需要手动设置创建时间和更新时间, 以及创建用户和更新用户
//        //设置创建时间和更新时间:本地时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //设置创建用户和更新用户:当前登录用户
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee); //调用save方法, 传入Employee对象, 保存到数据库中

        return R.success("新增员工{}成功" + employee.toString());
    }
    //http://localhost:8080/employee

    //分页查询员工列表, 需要构造分页构造器和条件构造器
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("分页查询: page = {},pageSize = {},name = {}", page, pageSize, name);

        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        //构造条件构造器queryWrapper
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>(); //创建一个LambdaQueryWrapper对象


        //添加过滤条件 : 当姓名非空, 则设置姓名的模糊查询条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name); //构造条件构造器, 设置name属性的模糊查询条件
        //添加排序条件 : 更新时间
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询

        employeeService.page(pageInfo, queryWrapper);


        return R.success(pageInfo);
    }
    //http://localhost:8080/employee/page?page=1&pageSize=10&name=张三


    //修改员工 - 这里是修改账号状态
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("线程id为：{}", id);

        //下面使用MybatisPlus的自动填充功能, 不再需要手动设置更新时间和更新用户
//        Long empId = (Long) request.getSession().getAttribute("employee"); //从Session中获取employee属性
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);

        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }
    //http://localhost:8080/employee

    //根据id查询员工信息, 地址栏变量
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }
    //http://localhost:8080/employee/1

} 
