package com.hanger.mapper;


import com.hanger.entity.Manager;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

@Component(value = "managerMapper")
public interface ManagerMapper extends Mapper<Manager> {

}
