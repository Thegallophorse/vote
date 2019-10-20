package com.hanger.mapper;


import com.hanger.entity.Candidate;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

@Component(value = "candidateMapper")
public interface CandidateMapper extends Mapper<Candidate> {
}
