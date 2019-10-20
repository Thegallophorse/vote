package com.hanger.mapper;


import com.hanger.entity.Vote;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

@Component(value = "voteMapper")
public interface VoteMapper extends Mapper<Vote> {
}
