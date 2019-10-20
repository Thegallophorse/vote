package com.hanger.service;


import com.hanger.entity.Voter;
import com.hanger.mapper.VoterMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class VoterService {
    private final VoterMapper voterMapper;

    @Autowired
    public VoterService(VoterMapper voterMapper) {
        this.voterMapper = voterMapper;
    }

    public Voter selectByPriKey(String uId) {
        return this.voterMapper.selectByPrimaryKey(uId);
    }

    public Integer insertSelective(Voter voter) {
        return this.voterMapper.insertSelective(voter);
    }

    public Integer deleteByPriKey(String id){
        return this.voterMapper.deleteByPrimaryKey(id);
    }

    public List<Voter> selectByExample(Object obj){
        return this.voterMapper.selectByExample(obj);
    }

    public Integer updateByPrimaryKeySelective(Voter voter) {
        return this.voterMapper.updateByPrimaryKeySelective(voter);
    }

    public Integer updateByExampleSelective(Voter voter , Example example) {
        return this.voterMapper.updateByExampleSelective(voter,example);
    }
}
