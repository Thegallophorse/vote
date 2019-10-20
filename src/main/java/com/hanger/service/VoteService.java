package com.hanger.service;


import com.hanger.entity.Vote;
import com.hanger.mapper.VoteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class VoteService {
    private final VoteMapper voteMapper;

    @Autowired
    public VoteService(VoteMapper voteMapper) {
        this.voteMapper = voteMapper;
    }

    public Integer insertSelective(Vote vote) {
        return this.voteMapper.insertSelective(vote);
    }

    public Vote selectByPriKey(String id) {
        return this.voteMapper.selectByPrimaryKey(id);
    }

    public int updateByPrimaryKeySelective(Vote vote) {
        return this.voteMapper.updateByPrimaryKeySelective(vote);
    }

    public List<Vote> selectByExample(Example example) {
        return this.voteMapper.selectByExample(example);
    }

    public Integer deleteByExample(Example example) {
        return voteMapper.deleteByExample(example);
    }


}
