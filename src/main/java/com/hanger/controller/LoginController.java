package com.hanger.controller;


import com.alibaba.fastjson.JSON;
import com.hanger.entity.Candidate;
import com.hanger.entity.Manager;
import com.hanger.entity.Vote;
import com.hanger.entity.Voter;
import com.hanger.service.CandidateService;
import com.hanger.service.ManagerService;
import com.hanger.service.VoteService;
import com.hanger.service.VoterService;
import com.hanger.util.JsonUtil;
import com.hanger.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@CrossOrigin
public class LoginController {
    private Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final ManagerService managerService;
    private final VoteService voteService;
    private final CandidateService candidateService;
    private final VoterService voterService;

    public LoginController(ManagerService managerService, VoteService voteService, CandidateService candidateService, VoterService voterService) {
        this.managerService = managerService;
        this.voteService = voteService;
        this.candidateService = candidateService;
        this.voterService = voterService;
    }


    //管理员登录入口
    @RequestMapping(value = "m_login",method = RequestMethod.POST)
    public String m_login(@RequestBody String str, HttpSession session) {
        logger.info("管理员" + str + "登录");
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"id","pwd"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String id = (String) map.get("id");
        String pwd = (String) map.get("pwd");

        //重复验证,这些工作虽然前端已经做过,但是为了更好的用户体验和安全还是再做一遍
        if (!(Pattern.compile("[A-Za-z0-9]{4,8}").matcher(id).matches())) {
            logger.info("管理员用户ID格式错误");
            return "{\"msg\":\"用户ID格式错误\",\"code\":\"001\"}";
        } else if (!(Pattern.compile("[A-Za-z0-9]{4,8}").matcher(pwd).matches())) {
            logger.info("管理员密码格式错误");
            return "{\"msg\":\"密码格式错误\",\"code\":\"010\"}";
        }

        Manager manager = managerService.selectByPriKey(id);
        if (manager == null) {
            logger.info("管理员用户ID不存在");
            return "{\"msg\":\"用户ID不存在\",\"code\":\"000\"}";
        } else if (!(pwd.equals(manager.getmPassword()))) {
            logger.info("管理员密码错误");
            return "{\"msg\":\"密码错误\",\"code\":\"011\"}";
        } else {
            session.setAttribute("LOGIN_USER",TokenUtil.createSession(id));
            logger.info("管理员" + id + "登录成功");
            if (isClean(id)) {
                return "{\"msg\":\"登录成功\",\"code\":\"110\"}";
            } else {
                return "{\"msg\":\"登录成功\",\"code\":\"111\"}";
            }
        }
    }



    //投票用户登录入口
    @RequestMapping(value = "u_login",method = RequestMethod.POST)
    public String u_login(@RequestBody String str, HttpSession session) {
        logger.info("投票用户" + str + "登录");
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"id","pwd"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String id = (String) map.get("id");
        String pwd = (String) map.get("pwd");

        //重复验证,这些工作虽然前端已经做过,但是为了更好的用户体验和安全还是再做一遍
        if (!(Pattern.compile("[A-Za-z0-9]{4,8}").matcher(id).matches())) {
            logger.info("投票用户的用户ID格式错误");
            return "{\"msg\":\"用户ID格式错误\",\"code\":\"001\"}";
        } else if (!(Pattern.compile("[A-Za-z0-9]{4,8}").matcher(pwd).matches())) {
            logger.info("投票用户的密码格式错误");
            return "{\"msg\":\"密码格式错误\",\"code\":\"010\"}";
        }

        Voter voter = voterService.selectByPriKey(id);
        if (voter == null) {
            logger.info("投票用户用户ID不存在");
            return "{\"msg\":\"用户ID不存在\",\"code\":\"000\"}";
        } else if (!(pwd.equals(voter.getvPassword()))){
            logger.info("投票用户密码错误");
            return "{\"msg\":\"密码错误\",\"code\":\"011\"}";
        } else {
            session.setAttribute("LOGIN_USER",TokenUtil.createSession(id));
            logger.info("投票用户" + id + "登录成功");
            return "{\"msg\":\"登录成功\",\"code\":\"111\"}";
        }
    }


    //判断该管理员是否有未清除的残留信息
    private Boolean isClean(String mid) {
        Example candidateExample = new Example(Candidate.class);
        candidateExample.createCriteria().andEqualTo("cMid",mid);
        List<Candidate> candidates = candidateService.selectByExample(candidateExample);
        if (candidates.size() > 0) {
            logger.warn("管理员" + mid + "还有上次登录的候选人信息");
            return false;
        }

        Example voteExample = new Example(Vote.class);
        voteExample.createCriteria().andEqualTo("vcMid",mid);
        List<Vote> list = voteService.selectByExample(voteExample);
        if (list.size() > 0) {
            logger.warn("管理员" + mid + "还有上次登录的投票信息");
            return false;
        }

        //置零管理员旗下所有投票人的状态信息
        //两种思路：我们出于干净考虑用第二种思路
        //       1、直接把所有存在的投票涉及的投票人状态清零
        //       2、把管理员旗下所有投票人状态信息不为零的记录全部置零
        //通用Example进行查询
        Example voterExample = new Example(Voter.class);
        voterExample.createCriteria().andEqualTo("vMid",mid).andNotEqualTo("vStatus",0);
        List<Voter> voters = voterService.selectByExample(voterExample);
        if (voters.size() > 0) {
            logger.warn("管理员" + mid + "还有上次登录的投票人信息");
            return false;
        }

        return true;
    }




}
