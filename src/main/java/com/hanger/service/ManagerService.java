package com.hanger.service;

import com.hanger.entity.Manager;
import com.hanger.mapper.ManagerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagerService {
    private final ManagerMapper managerMapper;

    @Autowired
    public ManagerService(ManagerMapper managerMapper) {
        this.managerMapper = managerMapper;
    }


    public Manager selectByPriKey(String uId) {
        return this.managerMapper.selectByPrimaryKey(uId);
    }

    public Integer updateByPrimaryKey(Manager manager) {
        return this.managerMapper.updateByPrimaryKey(manager);
    }
}
