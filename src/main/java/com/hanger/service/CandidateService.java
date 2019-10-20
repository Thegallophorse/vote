package com.hanger.service;


import com.hanger.entity.Candidate;
import com.hanger.mapper.CandidateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class CandidateService {
    private final CandidateMapper candidateMapper;

    @Autowired
    public CandidateService(CandidateMapper candidateMapper) {
        this.candidateMapper = candidateMapper;
    }

    public Integer insertSelective(Candidate candidate) {
        return this.candidateMapper.insertSelective(candidate);
    }

    public List<Candidate> selectByExample(Example example) {
        return this.candidateMapper.selectByExample(example);
    }

    public Integer deleteByExample(Example example) {
        return this.candidateMapper.deleteByExample(example);
    }

    public Candidate selectByPriKey(Long id) {
        return this.candidateMapper.selectByPrimaryKey(id);
    }

    public Integer updateByPrimaryKeySelective(Candidate trueCandidate) {
        return this.candidateMapper.updateByPrimaryKeySelective(trueCandidate);
    }

    public Integer deleteByPrimaryKey(Long id) {
        return this.candidateMapper.deleteByPrimaryKey(id);
    }

    public List<Candidate> selectAll() {
        return this.candidateMapper.selectAll();
    }

}
