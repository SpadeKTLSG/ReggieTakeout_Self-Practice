package com.tlsg.takeout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tlsg.takeout.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

//对应的Mapperxml文件位置: src/main/resources/mapper/EmployeeMapper.xml
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}
