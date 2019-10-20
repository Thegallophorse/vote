package com.hanger.controller;


import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.hanger.component.WebSocketServer;
import com.hanger.entity.Candidate;
import com.hanger.entity.Vote;
import com.hanger.entity.Voter;
import com.hanger.service.CandidateService;
import com.hanger.service.VoteService;
import com.hanger.service.VoterService;
import com.hanger.util.JsonUtil;
import com.hanger.util.LogAspectUtil;
import com.hanger.util.StrArrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;


@RestController
@CrossOrigin
public class VoteAgainController {
    private Logger logger = LoggerFactory.getLogger(VoteAgainController.class);

    private final VoteService voteService;
    private final VoterService voterService;
    private final CandidateService candidateService;
    public VoteAgainController(VoteService voteService, VoterService voterService, CandidateService candidateService) {
        this.voteService = voteService;
        this.voterService = voterService;
        this.candidateService = candidateService;
    }

    @Resource
    private WebSocketServer webSocket;


    //vote again...
    //再次投票的处理逻辑其实就是设置再次投票的候选人,和设置参与投票的投票人处理方法一致,只是需要多对新投票进行处理
    @RequestMapping(value = "voteAgain",method = RequestMethod.POST)
    public String voteAgain(@RequestBody String str) {
        logger.info("再次投票:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"vcid"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String vcid = (String) map.get("vcid");

        //判断该投票是否合法
        Vote vote = voteService.selectByPriKey(vcid);
        if (vote == null) {
            logger.warn("投票活动" + vcid + "不存在");
            return "{\"msg\":\"投票活动不存在\"}";
        }

        /*
        结算上一轮投票并计算出下一轮投票的候选人
         */

        //由于通用mapper不支持limit语法，这里借用分页实现查询
        logger.info("获取第" + vote.getVcWinnum() + "名候选人");
        PageHelper.startPage(Integer.parseInt(vote.getVcWinnum()), 1);
        //通用Example查询上一轮投票的所有候选人并按成绩倒序排序后结合怕个helper的第X名
        Example example = new Example(Candidate.class);
        //selectProperties一定要放在第一个才能生效
        example.selectProperties("cId","cSum");
        example.createCriteria()
                .andEqualTo("cMid",vote.getVcMid())
                .andEqualTo("cVcid",vcid);
        example.orderBy("cSum")
                .desc();
        List<Candidate> candidates = candidateService.selectByExample(example);
        //检验查询出来的结果是否有一条记录
        if (candidates.size() != 1) {
            logger.warn("没有找到分界名次");
            return "{\"msg\":\"没有找到分界名次\"}";
        }
        Candidate candidateLine = candidates.get(0);

        //判断是否需要进行二轮投票
        Example next = new Example(Candidate.class);
        //selectProperties一定要放在第一个才能生效
        next.selectProperties("cId");
        next.createCriteria()
                .andEqualTo("cSum",candidateLine.getcSum())
                .andEqualTo("cMid",vote.getVcMid())
                .andEqualTo("cVcid",vcid);
        List<Candidate> nextCandidates = candidateService.selectByExample(next);
        if (nextCandidates.size() == 1) {
            logger.warn("投票最终结果已经产出，不需要二轮投票了");
            return "{\"msg\":\"投票最终结果已经产出，不需要二轮投票了\"}";
        }

        //查询上轮投票获胜的
        Example win = new Example(Candidate.class);
        //selectProperties一定要放在第一个才能生效
        win.selectProperties("cId");
        win.createCriteria()
                .andGreaterThan("cSum",candidateLine.getcSum())
                .andEqualTo("cMid",vote.getVcMid())
                .andEqualTo("cVcid",vcid);
        List<Candidate> winCandidates = candidateService.selectByExample(win);

        int oldWinnum = Integer.parseInt(vote.getVcWinnum());
        if (oldWinnum == nextCandidates.size() + winCandidates.size()) {
            logger.warn("投票最终结果已经产出，不需要二轮投票了");
            return "{\"msg\":\"投票最终结果已经产出，不需要二轮投票了\"}";
        }

        String[] winIds = new String[winCandidates.size()];
        for (int i = 0; i < winCandidates.size(); i++) {
            winIds[i] = Long.toString(winCandidates.get(i).getcId());
        }
        String winCids = JSON.toJSONString(winIds);
        if (voteService.updateByPrimaryKeySelective(new Vote(vcid, winCids)) == 1) {
            logger.info("上轮投票获胜的名单已经保存成功");
        }

        //注:除候选人、本轮胜出人数（开始与本轮可选赞成票相等）、胜出id外的其他一切信息将沿用上一轮的
        logger.info( "正在初始化再次投票的信息......");

        //切割字符串vcid获取基本信息
        String[] mid_num = vote.getVcId().split("_");
        String mid = mid_num[0];
        String num = mid_num[1];
        logger.info("分割vcid后取出的次数" + num);
        //对次数进行加一操作
        int i = Integer.parseInt(num);
        i = i +1;
        //与前一步拼接生成下一次唯一id
        String id = mid + "_" + i;
        vote.setVcId(id);
        logger.info("本轮投票唯一id:" + id);

        String newWinnum = Integer.toString(oldWinnum - winCandidates.size());
        vote.setVcWinnum(newWinnum);
        logger.info("本轮投票胜选人数:" + newWinnum);

        int allowNum;
        if (!(vote.getVcMode().equals("11"))) {
            allowNum = nextCandidates.size();
        } else {
            allowNum = oldWinnum - winCandidates.size();
        }
        vote.setVcAllownum(Integer.toString(allowNum));
        logger.info("投票人允许评判的个数:" + allowNum);

        String ids = vote.getVcVids();
        String[] vids = StrArrUtil.stringToArray(ids);
        //判断(vids == null),这里其实可以不用判断,因为既然能存入数据库,说明之前肯定不是空
        if (vids == null) {
            logger.warn("vids不是字符串");
            return "{\"msg\":\"不是字符串\"}";
        }

        logger.info("开始初始化投票人允许评判的(赞成票数)个数...");
        //记录初始化失败的个数
        int count = 0;
        for (String vid:
             vids) {
            Voter voter = voterService.selectByPriKey(vid);
            voter.setvStatus(allowNum);
            //其他一样的属性置null
            voter.setvPassword(null);
            voter.setvName(null);
            voter.setvMid(null);
            if (voterService.updateByPrimaryKeySelective(voter) == 1) {
                logger.info("初始化投票人" + voter.getvId() + "允许评判的个数成功");
            } else {
                logger.info("初始化投票人" + voter.getvId() + "失败,数据库执行失败");
                ++count;
            }
        }
        if (count == 0) {
            logger.info("初始化投票人允许评判的个数成功");
        } else {
            logger.info("数据库执行失败,本次投票的结果将不再准确");
            /*
              LogAspectUtil.LOG_SQLEXE_EORROR
              只要出现了这种错误，本次投票的结果将不再准确
              注：其实上面这个foreach循环换成for循环可以执行回滚,如果回滚失败数据库sql操作真的是出问题了
             */
            System.out.println(LogAspectUtil.LOG_SQLEXE_EORROR);
            return "{\"msg\":\"数据库执行失败\"}";
        }

        String[] nextIds = new String[nextCandidates.size()];
        for (int j = 0; j < nextCandidates.size(); j++) {
            nextIds[j] = Long.toString(nextCandidates.get(j).getcId());
        }
        String nextCids = JSON.toJSONString(nextIds);
        vote.setVcCids(nextCids);

        //复用count记录候选人初始化失败个数
        count = 0;
        logger.info("本次参与投票的候选人id如下：");
        for (String cid : nextIds) {
            logger.info("初始化候选人:" + cid);
            long cidTemp = Long.parseLong(cid);
            Candidate candidate = candidateService.selectByPriKey(cidTemp);
            if (candidate == null) {
                logger.warn("候选人" + cid + "不存在");
                return "{\"msg\":\"候选人" + cid + "不存在\"}";
            }
            //置零总分并且切换外键vcid
            candidate.setcInfo(null);
            candidate.setcMid(null);
            candidate.setcSum(0L);
            candidate.setcAbstention(0);
            candidate.setcDissenting(0);
            candidate.setcVcid(id);
            if (candidateService.updateByPrimaryKeySelective(candidate) == 1) {
                logger.info("初始化候选人" + candidate.getcId() + "成功");
            } else {
                logger.info("候选人" + candidate.getcId() + "初始化失败！");
                ++count;
            }
        }
        if (count == 0) {
            logger.info("初始化候选人成功！");
        } else {
            logger.info("数据库执行失败,本次投票的结果将不再准确");
            System.out.println(LogAspectUtil.LOG_SQLEXE_EORROR);
            return "{\"msg\":\"数据库执行失败\"}";
        }

        //通用mapper只要调用就会执行,执行成功返回执行成功的个数(这里是1)
        if (voteService.insertSelective(vote) == 1) {
            logger.info("管理员:" + mid + "成功初始化第" + id + "轮投票" + id);
            //将最新的消息通知已经登录的投票端小伙伴
            webSocket.sendMessage("系统发现您有新的投票活动！");
            return "{\"msg\":\"成功初始化第" + id + "轮投票\",\"code\":\"111\"}";
        }else {
            logger.info("数据库执行失败,本次投票的结果将不再准确");
            System.out.println(LogAspectUtil.LOG_SQLEXE_EORROR);
            return "{\"msg\":\"数据库执行失败\"}";
        }
    }





}
