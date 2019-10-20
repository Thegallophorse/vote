package com.hanger.mapper;


import com.hanger.entity.Voter;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

@Component(value = "voterMapper")
public interface VoterMapper extends Mapper<Voter> {
}
