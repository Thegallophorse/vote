package com.hanger.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hanger.component.WebSocketServer;
import com.hanger.entity.*;
import com.hanger.service.CandidateService;
import com.hanger.service.ManagerService;
import com.hanger.service.VoteService;
import com.hanger.service.VoterService;
import com.hanger.util.JsonUtil;
import com.hanger.util.LogAspectUtil;
import com.hanger.util.StrArrUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;


@RestController
@CrossOrigin
public class VoteController {
    private Logger logger = LoggerFactory.getLogger(VoteController.class);

    private final ManagerService managerService;
    private final VoteService voteService;
    private final CandidateService candidateService;
    private final VoterService voterService;
    @Autowired
    public VoteController(ManagerService managerService, VoteService voteService, CandidateService candidateService, VoterService voterService) {
        this.managerService = managerService;
        this.voteService = voteService;
        this.candidateService = candidateService;
        this.voterService = voterService;
    }

    @Resource
    private WebSocketServer webSocket;



    //管理员设置自己候选人的属性和文件属性
    @RequestMapping(value = "setFields",method = RequestMethod.POST)
    public String setFields(@RequestBody String str) {
        logger.info("管理员发过来的字段" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"id","fields","files"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String mid = (String) map.get("id");
        String fds = (String) map.get("fields");
        String fs = (String) map.get("files");

        Manager manager = managerService.selectByPriKey(mid);
        //判断该管理员是否存在
        if (manager == null) {
            logger.warn("该管理员不存在");
            return "{\"msg\":\"该管理员不存在\"}";
        }

        String [] fields = StrArrUtil.stringToArray(fds);

        //判断该属性组是否可用
        if ((fields == null) || (fields.length == 0)) {
            logger.warn("属性组不可用");
            return "{\"msg\":\"属性组不可用\"}";
        }

        //判断是否有正在进行的投票
        Example example = new Example(Vote.class);
        example.createCriteria()
                .andEqualTo("vcMid",mid);
        List<Vote> list = voteService.selectByExample(example);
        if (list.size() > 0) {
            logger.warn("管理员" + mid + "有投票正在进行,不能设置属性");
            return "{\"msg\":\"您有投票正在进行,请先结束投票\"}";
        }

        HashMap<String, String> hashMap = new HashMap<>();
        //属性组从0开始存
        for (int i = 0;i < fields.length; i++){
            String key = Integer.toString(i);
            hashMap.put(key,fields[i]);
        }
        String mFields = JSON.toJSONString(hashMap);

        //初始化文件属性组
        String [] files;
        if (fs != null) {
            files = StrArrUtil.stringToArray(fs);
            if ((files != null) && (files.length > 0)) {
                //清空hashMap实现复用
                hashMap.clear();
                //文件属性组从0开始存
                for (int i = 0;i < files.length; i++){
                    String key = Integer.toString(i);
                    hashMap.put(key,files[i]);
                }
                String mFiles = JSON.toJSONString(hashMap);
                manager.setmFiles(mFiles);
                //启动静默非pdf文件格式转换程序转换D:/Public下一级目录文件
                // 虽然提升了用户的速度但是会导致管理员等待时间增加（不推荐）
//                ArrayList<String> filenames = FileUtil.getFilenames("D://Public");
//                if (filenames != null) {
//                    for (String fname : filenames) {
//                        convert2Pdf(fname);
//                    }
//                }
            } else {
                manager.setmFiles(null);
            }
        } else {
            manager.setmFiles(null);
        }

        manager.setmFields(mFields);
        //通用mapper只要调用就会执行,执行成功返回整型1
        if (managerService.updateByPrimaryKey(manager) == 1){
            logger.info("管理员:" + mid + "成功设置属性");
            return "{\"msg\":\"111\"}";
        }else {
            return "{\"msg\":\"数据库执行失败\"}";
        }
    }



    //fist vote...
    //设置投票基本信息
    @RequestMapping(value = "setVoteBase",method = RequestMethod.POST)
    public String setVoteBase(@RequestBody String str) {
        logger.info("设置投票基本信息:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"mid","sn","tn","theme","mode","winnum"};
        if (!(JsonUtil.checkFormat(str , jsonKeys , 7))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String mid = (String) map.get("mid");
        String sname = (String) map.get("sn");
        String tname = (String) map.get("tn");
        String theme = (String) map.get("theme");
        String mode = (String) map.get("mode");
        String winnum = (String) map.get("winnum");

        //判断该管理员是否存在
        if (managerService.selectByPriKey(mid) == null) {
            logger.warn("{\"msg\":\"该管理员不存在\"}");
            return "{\"msg\":\"该管理员不存在\"}";
        }

        //判断该模式是否合法
        if ((!(mode.equals("11"))) && (!(mode.equals("00"))) && (!(mode.equals("01"))) && (!(mode.equals("02")))) {
            logger.warn("模式" + mode + "不存在");
            return "{\"msg\":\"模式不存在\"}";
        }

        //如果允许的值为""，则认为可投次数为本次投票所有候选人的人数
        String allownum = "";
        if (map.containsKey("anum")) {
            allownum = (String) map.get("anum");
        }
        //判断基本内容的合法性
        if ((sname.length() <= 0) || (sname.length() > 100)) {
            logger.warn("监票人姓名不合法");
            return "{\"msg\":\"监票人姓名不合法\"}";
        } else if ((tname.length() <= 0) || (tname.length() > 100)) {
            logger.warn("计票人姓名不合法");
            return "{\"msg\":\"计票人姓名不合法\"}";
        } else if ((theme.length() <= 0) || (theme.length() > 100)) {
            logger.warn("投票主题名程不合法");
            return "{\"msg\":\"投票主题名程不合法\"}";
        }

        //通用Example进行查询
        Example example = new Example(Voter.class);
        example.createCriteria().andEqualTo("vMid",mid).andEqualTo("vStatus",-1);
        List<Voter> voters = voterService.selectByExample(example);
        if (voters.size() == 0) {
            logger.info("投票人人数为不能为0");
            return "{\"msg\":\"投票人人数为不能为0\"}";
        }

        List<Candidate> candidates = candidateService.selectAll();
        if (candidates.size() == 0) {
            logger.info("候选人人数为不能为0");
            return "{\"msg\":\"候选人人数为不能为0\"}";
        } else if ((!(Pattern.compile("[0-9]*").matcher(winnum).matches())) || (Integer.parseInt(winnum) >= candidates.size()) || (Integer.parseInt(winnum) <= 0)) {
            logger.info("允许胜选的数目错误");
            return "{\"msg\":\"允许胜选的数目错误\"}";
        }

        logger.info( "正在初始化投票...");
        Vote vote = new Vote();

        String id = mid + "_" + "1";
        logger.info("本次投票唯一id:" + id);
        vote.setVcId(id);
        logger.info("管理员id:" + mid);
        vote.setVcMid(mid);
        logger.info("监票人:" + sname);
        vote.setVcSname(sname);
        logger.info("计票人:" + tname);
        vote.setVcTname(tname);
        logger.info("投票名称:" + theme);
        vote.setVcTheme(theme);
        logger.info("投票模式:" + mode);
        vote.setVcMode(mode);

        int count = 0;//记录初始化失败的个数
        if (allownum.equals("")) {
            logger.info("投票人允许评判的个数:候选人的总人数");
            vote.setVcAllownum(Integer.toString(candidates.size()));
            for (Voter v:
                 voters) {
                v.setvStatus(candidates.size());
                v.setvPassword(null);
                v.setvName(null);
                v.setvMid(null);
                if (voterService.updateByPrimaryKeySelective(v) == 1) {
                    logger.info("初始化投票人" + v.getvId() + "允许评判的个数成功");
                } else {
                    logger.info("初始化投票人" + v.getvId() + "失败,数据库执行失败");
                    ++count;
                }
            }
            if (count == 0) {
                logger.info("投票人允许评判的个数初始化成功！");
            } else {
                logger.info("投票人允许评判的个数初始化失败！");
                return "{\"msg\":\"投票人允许评判的个数初始化失败\"}";
            }
        } else {
            if ((!(Pattern.compile("[0-9]*").matcher(allownum).matches())) || (Integer.parseInt(allownum) >= candidates.size()) || (Integer.parseInt(allownum) <= 0)) {
                logger.info("允许评判的候选人数目错误");
                return "{\"msg\":\"允许评判的候选人数目错误\"}";
            }

            logger.info("投票人允许评判的个数:" + allownum);
            vote.setVcAllownum(allownum);
            for (Voter v:
                    voters) {
                v.setvStatus(Integer.parseInt(allownum));
                v.setvPassword(null);
                v.setvName(null);
                v.setvMid(null);
                if (voterService.updateByPrimaryKeySelective(v) == 1) {
                    logger.info("初始化投票人" + v.getvId() + "允许评判的个数成功");
                } else {
                    logger.info("初始化投票人" + v.getvId() + "失败,数据库执行失败");
                    ++count;
                }
            }
            if (count == 0) {
                logger.info("投票人允许评判的个数初始化成功！");
            } else {
                logger.info("投票人允许评判的个数初始化失败！");
                return "{\"msg\":\"投票人允许评判的个数初始化失败\"}";
            }
        }

        logger.info("本次参与投票的投票人名单如下：");
        String [] vids = new String[voters.size()];
        for (int vnum = 0;vnum < voters.size();vnum++) {
            vids[vnum] = voters.get(vnum).getvId();
            logger.info("投票人id:" + vids[vnum] + ",name：" + voters.get(vnum).getvName());
        }
        String rvids = JSON.toJSONString(vids);
        logger.info("投票人id组json后存储的数组串:" + rvids);
        vote.setVcVids(rvids);

        logger.info("本次投票胜选的人数:" + winnum);
        vote.setVcWinnum(winnum);

        logger.info("本次参与投票的候选人如下：");
        String [] cids = new String[candidates.size()];
        //复用count记录候选人初始化失败个数
        count = 0;
        for (int cnum = 0;cnum < candidates.size();cnum++) {
            cids[cnum] = String.valueOf(candidates.get(cnum).getcId());
            //更新候选人vcid外键,这里保险起见把sum、abstention、dissenting也重新置零了
            candidates.get(cnum).setcSum(0L);
            candidates.get(cnum).setcAbstention(0);
            candidates.get(cnum).setcDissenting(0);
            candidates.get(cnum).setcMid(null);
            candidates.get(cnum).setcInfo(null);
            candidates.get(cnum).setcVcid(id);
            if (candidateService.updateByPrimaryKeySelective(candidates.get(cnum)) == 1) {
                logger.info("初始化候选人" + candidates.get(cnum).getcId() + "的cvid成功");
            } else {
                logger.info("候选人" + candidates.get(cnum).getcId() + "的cvid初始化失败！");
                ++count;
            }
        }
        if (count == 0) {
            logger.info("初始化候选人成功！");
        } else {
            logger.info("初始化候选人失败！");
            return "{\"msg\":\"候选人的cvid初始化失败\"}";
        }

        String rcids = JSON.toJSONString(cids);
        logger.info("候选人id组json后存储的数组串:" + rcids);
        vote.setVcCids(rcids);

        Vote oldVote = voteService.selectByPriKey(id);
        if (oldVote == null) {
            //通用mapper只要调用就会执行,执行成功返回执行成功的个数(这里是1)
            if (voteService.insertSelective(vote) == 1) {
                logger.info("管理员:" + mid + "成功初始化第1轮投票" + id);
                //将最新的消息通知已经登录的投票端小伙伴
                webSocket.sendMessage("系统发现您有新的投票活动！");
                return "{\"msg\":\"成功初始化第一轮投票\"}";
            } else {
                logger.info("数据库执行失败");
                return "{\"msg\":\"数据库执行失败\"}";
            }
        } else {
            //通用mapper只要调用就会执行,执行成功返回执行成功的个数(这里是1)
            if (voteService.updateByPrimaryKeySelective(vote) == 1) {
                logger.info("管理员:" + mid + "成功初始化第1轮投票" + id);
                //将最新的消息通知已经登录的投票端小伙伴
                webSocket.sendMessage("系统发现您有新的投票活动！");
                return "{\"msg\":\"成功初始化第一轮投票\"}";
            }else {
                logger.info("数据库执行失败");
                return "{\"msg\":\"数据库执行失败\"}";
            }
        }
    }



    //设置参与投票的投票人
    @RequestMapping(value = "selectVoters",method = RequestMethod.POST)
    public String selectVoters(@RequestBody String str) {
        logger.info("设置参与投票的投票人receive:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"id","arr"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String mid = (String) map.get("id");
        String ids = JSON.toJSONString(map.get("arr"));
        //判断该管理员是否存在
        if (managerService.selectByPriKey(mid) == null) {
            logger.warn("{\"msg\":\"该管理员不存在\"}");
            return "{\"msg\":\"该管理员不存在\"}";
        }

        //判断是否存在投票活动
        Example example = new Example(Vote.class);
        example.createCriteria().andEqualTo("vcMid",mid);
        List<Vote> votes = voteService.selectByExample(example);
        if (votes.size() != 0) {
            logger.warn("管理员" + mid + "有投票正在进行");
            return "{\"msg\":\"有投票正在进行中，无法执行选择操作\"}";
        }

        String[] vids = StrArrUtil.stringToArray(ids);
        //判断该属性组是否可用
        if ((vids == null) || (vids.length == 0)) {
            logger.warn("投票人id组为空");
            return "{\"msg\":\"投票人id组arr不可用\"}";
        }

        //存储每个id执行后的状态码code
        String [] codes = new String[vids.length];
        //记录设置成功的个数
        int count = 0;
        for (int i = 0;i < vids.length;i++) {

            Voter voter = voterService.selectByPriKey(vids[i]);
            if (voter == null) {
                logger.warn("投票人" + vids[i] + "不存在");
                codes[i] = "000";
            } else {
                voter.setvStatus(-1);
                voter.setvPassword(null);
                voter.setvName(null);
                voter.setvMid(null);
                if (voterService.updateByPrimaryKeySelective(voter) == 1) {
                    logger.info("管理员:" + mid + "成功设置投票人" + vids[i]);
                    codes[i] = "111";
                    ++count;
                } else {
                    logger.warn("投票人" + vids[i] + "不存在或数据库执行失败");
                    codes[i] = "000";
                }
            }
        }
        logger.info("共:" + vids.length + "条,设置成功:" + count + "条,设置失败:" + (vids.length - count) + "条");

        String msg = JSON.toJSONString(codes);
        return "{\"msg\":" + msg + "}";
    }



    //管理员取消投票人投票的资格
    @RequestMapping(value = "unSelectVoters",method = RequestMethod.POST)
    public String unSelectVoters(@RequestBody String str) {
        logger.info("取消投票人投票的资格:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"id","arr"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String mid = (String) map.get("id");
        String ids = JSON.toJSONString(map.get("arr"));
        //判断该管理员是否存在
        if (managerService.selectByPriKey(mid) == null) {
            logger.warn("{\"msg\":\"该管理员不存在\"}");
            return "{\"msg\":\"该管理员不存在\"}";
        }

        String[] vids = StrArrUtil.stringToArray(ids);
        //判断该属性组是否可用
        if ((vids == null) || (vids.length == 0)) {
            logger.warn("投票人id组为空");
            return "{\"msg\":\"投票人id组arr不可用\"}";
        }

        //存储每个id执行后的状态码code
        String [] codes = new String[vids.length];
        //记录设置成功的个数
        int count = 0;
        for (int i = 0;i < vids.length;i++) {
            Voter voter = voterService.selectByPriKey(vids[i]);
            if (voter == null) {
                logger.warn("投票人" + vids[i] + "不存在");
                codes[i] = "000";
            } else if (voter.getvStatus() > 0) {
                logger.info("管理员:" + mid + "删除的投票人" + voter.getvId() + "正在参与投票");
                codes[i] = "001";
            } else {
                voter.setvStatus(0);
                voter.setvPassword(null);
                voter.setvName(null);
                voter.setvMid(null);
                if (voterService.updateByPrimaryKeySelective(voter) == 1) {
                    logger.info("管理员:" + mid + "成功取消投票人" + vids[i] + "投票资格");
                    codes[i] = "111";
                    ++count;
                } else {
                    logger.warn("取消投票人" + vids[i] + "投票的资格失败数据库执行失败");
                    codes[i] = "000";
                }
            }
        }
        logger.info("共:" + vids.length + "条,取消成功:" + count + "条,取消失败:" + (vids.length - count) + "条");

        String msg = JSON.toJSONString(codes);
        return "{\"msg\":" + msg + "}";
    }



    //获取本次参与投票的投票人的分页数据
    @RequestMapping(value = "getVoteVoters",method = RequestMethod.POST)
    public String getVoteVoters(@RequestBody String str) {
        logger.info("发过来的信息：" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"id","pn"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String id = (String) map.get("id");
        String spn = (String) map.get("pn");

        //判断该管理员是否存在
        if (managerService.selectByPriKey(id) == null) {
            logger.warn("{\"msg\":\"该管理员不存在\"}");
            return "{\"msg\":\"该管理员不存在\"}";
        }
        //判断页码是否合法
        if (!(Pattern.compile("[0-9]*").matcher(spn).matches()) || (spn.equals(""))){
            return "{\"msg\":\"该页码不存在\"}";
        }
        int pn = Integer.parseInt(spn);
        if (pn <= 0){
            return "{\"msg\":\"该页码不存在\"}";
        }

        logger.info("获取管理员：" + id + "的第" + pn + "页本次投票的投票人");
        //Mapper接口方式的调用PageHelper（推荐）
        // 只有紧跟在PageHelper.startPage方法后的第一个Mybatis的查询（Select）方法会被分页
        PageHelper.startPage(pn, 20);

        //通用Example进行查询
        Example example = new Example(Voter.class);
        example.createCriteria().andEqualTo("vMid",id).andNotEqualTo("vStatus", 0);
        List<Voter> voters = voterService.selectByExample(example);
        //logger.info("对象list集合" + voters);
        String vs = JSON.toJSONString(voters);
        //logger.info("Json后的字符串" + vs);

        //用PageInfo对结果进行包装
        PageInfo<Voter> info = new PageInfo<>(voters);
        //PageInfo包含了非常全面的分页属性
        logger.info("总页数" + info.getPages() + "页");
        logger.info("前一页是第" + info.getPrePage() + "页");
        logger.info("当前页码第" + info.getPageNum() + "页");
        logger.info("后一页是第" + info.getNextPage() + "页");
        logger.info("总记录数" + info.getTotal() + "条");
        logger.info("每页有" + info.getPageSize() + "条记录");
        logger.info("当前页有" + info.getSize() + "条记录");
        logger.info("当前显示数据库中行号的" + info.getStartRow() + "行~" + info.getEndRow() + "行共" + info.getSize() + "条数据");

//        logger.info("是否为第一页" + info.isIsFirstPage());
//        logger.info("是否为最后一页" + info.isIsLastPage());
//        logger.info("是否还有前一页" + info.isHasPreviousPage());
//        logger.info("是否还有后一页" + info.isHasNextPage());

        String tp = Integer.toString(info.getPages());
        String td = Long.toString(info.getTotal());
        logger.info("{\"msg\":\"111\",\"tp\":" + tp + ",\"td\":" + td + ",\"info\":" + vs + "}");
        return "{\"msg\":\"111\",\"tp\":" + tp + ",\"td\":" + td + ",\"info\":" + vs + "}";
    }



    //投票人获取投票活动列表
    @RequestMapping(value = "getVotes",method = RequestMethod.POST)
    public String getVotes(@RequestBody String str) {
        logger.info("投票人获取投票活动列表:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"id"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String vid = (String) map.get("id");

        //判断该投票人是否合法
        Voter voter = voterService.selectByPriKey(vid);
        if (!(isRule(voter).equals(""))) {
            return isRule(voter);
        }

        String mid = voter.getvMid();
        //通用Example进行查询
        Example example = new Example(Vote.class);
        example.createCriteria().andEqualTo("vcMid",mid);
        List<Vote> votes = voteService.selectByExample(example);

        if (votes.size() == 0) {
            logger.warn("投票人" + vid + "的管理员" + mid + "还未发起投票");
            return "{\"msg\":\"投票人的管理员还未发起投票\"}";
        }

        logger.info("原生对象list集合" + votes);
        //将不需要转json的属性置为null后fastjson默认就不会转换
        for (Vote vote : votes) {
            vote.setVcMid(null);
            vote.setVcVids(null);
            vote.setVcCids(null);
            vote.setVcWincids(null);
        }
        logger.info("不需要的属性置为null后的对象list集合" + votes);
        String vs = JSON.toJSONString(votes);
        logger.info("Json后的字符串" + vs);

        logger.info("{\"msg\":\"111\",\"info\":" + vs + "}");
        return "{\"msg\":\"111\",\"info\":" + vs + "}";
    }



    //投票人获取公告信息
    @RequestMapping(value = "getNotice",method = RequestMethod.POST)
    public String getNotice(@RequestBody String str) {
        logger.info("投票人获取公告信息:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"vid"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String vcid = (String) map.get("vid");

        Vote vote = voteService.selectByPriKey(vcid);
        logger.info("原生vote对象" + vote);
        if (vote == null) {
            logger.warn("投票活动" + vcid + "不存在");
            return "{\"msg\":\"投票活动不存在\"}";
        }

        //将不需要转json的属性置为null后fastjson默认就不会转换
        vote.setVcId(null);
        vote.setVcMid(null);
        vote.setVcCids(null);
        vote.setVcVids(null);
        vote.setVcWincids(null);
        logger.info("不需要的属性置为null后的对象" + vote);
        String v = JSON.toJSONString(vote);
        logger.info("Json后的字符串" + v);

        logger.info("{\"msg\":\"111\",\"info\":" + v + "}");
        return "{\"msg\":\"111\",\"info\":" + v + "}";
    }



    //投票人获取投票活动的候选人列表
    //这里是取出当时存进投票实体vote的候选人id属性组的id一个个便利取出返回的
    @RequestMapping(value = "getVoteCandidates",method = RequestMethod.POST)
    public String getVoteCandidates(@RequestBody String str) {
        logger.info("投票人获取投票活动的候选人列表:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"vid","vcid"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String vid = (String) map.get("vid");
        String vcid = (String) map.get("vcid");

        //判断该投票人是否合法
        Voter voter = voterService.selectByPriKey(vid);
        if (!(isRule(voter).equals(""))) {
            return isRule(voter);
        }

        String mid = voter.getvMid();
        //判断该管理员是否存在
        Manager manager = managerService.selectByPriKey(mid);
        if (manager == null) {
            logger.warn("{\"msg\":\"该管理员不存在\"}");
            return "{\"msg\":\"该管理员不存在\"}";
        }

        //判断该投票是否合法
        Vote vote = voteService.selectByPriKey(vcid);
        if (vote == null) {
            logger.warn("投票活动" + vcid + "不存在");
            return "{\"msg\":\"投票活动不存在\"}";
        }
        String allownum = vote.getVcAllownum();

        logger.info("原生vote对象" + vote);
        String mode = vote.getVcMode();

        //将vote的候选人数组字符串转换为数组
        String[] cids = StrArrUtil.stringToArray(vote.getVcCids());
        if (cids == null) {
            logger.warn("cids不是字符串");
            return "{\"msg\":\"不是字符串\"}";
        }

        StringBuilder cs = new StringBuilder("[");

        for (String cid : cids) {
            long id = Long.parseLong(cid);
            //这里如果没人私自改数据库的vote的候选人id数组，100%不会出问题,所以不再判断候选人id是否存在
            Candidate candidate = candidateService.selectByPriKey(id);
            if (candidate == null) {
                logger.warn("数据库未经许可被更改导致候选人id不匹配");
                return "{\"msg\":\"数据库未经许可被更改导致候选人id不匹配\"}";
            }
            cs.append("{\"cId\":").append(candidate.getcId()).append(",\"cVcid\":\"").append(candidate.getcVcid()).append("\",\"cInfo\":").append(candidate.getcInfo()).append("},");
        }

        //去除最后一个逗号
        int end = cs.length();
        cs = new StringBuilder(cs.substring(0, (end - 1)));
        cs.append("]");
        logger.info("拼装后的字符串" + cs);

        //获取文件属性的个数
        int filenum = 0;
        String files = manager.getmFiles();
        if (files != null) {
            HashMap fsMap = JSON.parseObject(files , HashMap.class);
            filenum = fsMap.size();
        }

        logger.info("{\"msg\":\"111\",\"mode\":\"" + mode + "\",\"anum\":\"" + allownum + "\",\"flen\":" + filenum + ",\"info\":" + cs + "}");
        return "{\"msg\":\"111\",\"mode\":\"" + mode + "\",\"anum\":\"" + allownum + "\",\"flen\":" + filenum + ",\"info\":" + cs + "}";
    }



    //参与投票的投票人提交投票结果（提交结果给管理员的最新的投票事件）
    @RequestMapping(value = "subVoteResult",method = RequestMethod.POST)
    public String subVoteResult(@RequestBody String str) {
        logger.info("参与投票的投票人提交结果receive:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"vid","arr"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String vid = (String) map.get("vid");
        String cs = JSON.toJSONString(map.get("arr"));
        //System.out.println("发过来的数组" + cs);

        Voter voter = voterService.selectByPriKey(vid);
        //判断该投票人是否合法
        if (!(isRule(voter).equals(""))) {
            return isRule(voter);
        }

        List<Mark> marks = JSON.parseArray(cs, Mark.class);
        //System.out.println("转换成marks实体类" + marks);
        //检验arr中是否有null(有null的话说明有符合规定的提交)
        for (Mark m : marks) {
            if (m == null) {
                logger.warn("投票人" + vid + "提交的结果中有不合法的值");
                return "{\"msg\":\"提交的结果中有不合法的值\"}";
            }
        }

        //根据当前投票人的id（vid）的外键mid获取最新的投票事件
        String vcid;
        int i = 1;
        while (true) {
            vcid = voter.getvMid() + "_" + i;
            if (voteService.selectByPriKey(vcid) == null) {
                break;
            }
            i++;
        }
        //判断投票次数的合法性
        if (i == 1) {
            logger.warn("管理员" + voter.getvMid() + "还未发起任何投票");
            return "{\"msg\":\"还未发起任何投票\"}";
        }
        //对次数进行减一操作
        i = i - 1;
        logger.info("从vcid取出的次数" + i);
        //与mid拼接生成vcid
        vcid = voter.getvMid() + "_" + i;
        logger.info("对应的投票id:" + vcid);
        //获取投票事件的对象
        Vote vote = voteService.selectByPriKey(vcid);
        String[] cids = StrArrUtil.stringToArray(vote.getVcCids());
        if (cids == null) {
            logger.warn("cids不是字符串");
            return "{\"msg\":\"不是字符串\"}";
        }

        //判断票数的合法性
        //判断投的票数是否大于候选人总数
        if (marks.size() > cids.length) {
            logger.warn("投票人" + vid + "投的票数大于候选人总数");
            return "{\"msg\":\"投的票数大于候选人总数\"}";
        }
        //判断拥有的赞成票数是否大于所投的赞成人数
        if (voter.getvStatus() > Integer.parseInt(vote.getVcAllownum())) {
            logger.warn("投票人" + vid + "投的赞成票数超过允许范围");
            return "{\"msg\":\"赞成票数超过允许范围\"}";
        }

        //判断候选人id与投票类型是否合法(必须是00(反对)/01(弃权)/11(赞成)三种之一)
        for (Mark mark:
                marks) {
            long cid = Long.parseLong(Integer.toString(mark.getcId()));
            //判断候选人id是否合法
            Candidate candidate = candidateService.selectByPriKey(cid);
            if (candidate == null) {
                logger.warn("投票人" + vid + "所投候选人" + cid + "的id不存在");
                return "{\"msg\":\"有候选人id不存在\"}";
            }

            //判断投票类型是否合法
            String score = mark.getcScore();
            if (!(Pattern.compile("[0-9]*").matcher(score).matches()) || (score.equals("")) || ((!(score.equals("11"))) && (!(score.equals("01"))) && (!(score.equals("00"))))) {
                logger.warn("投票人" + vid + "所投候选人" + cid + "的投票类型不在要求范围(必须是00(反对)/01(弃权)/11(赞成)三种之一)");
                return "{\"msg\":\"有投票类型不在要求范围(必须是(反对)/(弃权)/(赞成)三种之一)\"}";
            }
        }

        //将投票人的票数置为零
        voter.setvMid(null);
        voter.setvName(null);
        voter.setvPassword(null);
        voter.setvStatus(0);
        if (voterService.updateByPrimaryKeySelective(voter) == 1) {
            logger.info("投票人:" + vid + "开始投票了！！！");
        } else {
            logger.warn("数据库执行投票人减票失败");
            return "{\"msg\":\"数据库执行失败\"}";
        }

        if (marks.size() == 0) {
            logger.warn("投票人" + vid + "决定不选择任何意见直接提交");
            return "{\"msg\":\"成功完成投票\"}";
        }

        //开始正常的业务...
        //给候选人加票,必须是00(反对)/01(弃权)/11(赞成)三种之一
        //记录设置成功的个数
        int count = 0;
        for (Mark mark:
                marks) {
            long cid = Long.parseLong(Integer.toString(mark.getcId()));
            Candidate candidate = candidateService.selectByPriKey(cid);
            candidate.setcInfo(null);
            candidate.setcMid(null);
            candidate.setcVcid(null);
            String s = mark.getcScore();
            switch (s) {
                case "00":
                    candidate.setcSum(null);
                    candidate.setcAbstention(null);
                    int dissenting = candidate.getcDissenting();
                    candidate.setcDissenting(dissenting + 1);
                    if (candidateService.updateByPrimaryKeySelective(candidate) == 1) {
                        logger.info("投票人:" + vid + "成功投票给候选人" + cid);
                    } else {
                        logger.warn("数据库执行投票人:" + vid + "给候选人" + cid + "投票失败");
                        ++count;
                    }
                    break;
                case "01":
                    candidate.setcSum(null);
                    candidate.setcDissenting(null);
                    int abstention = candidate.getcAbstention();
                    candidate.setcAbstention(abstention + 1);
                    if (candidateService.updateByPrimaryKeySelective(candidate) == 1) {
                        logger.info("投票人:" + vid + "成功投票给候选人" + cid);
                    } else {
                        logger.warn("数据库执行投票人:" + vid + "给候选人" + cid + "投票失败");
                        ++count;
                    }
                    break;
                default:
                    candidate.setcAbstention(null);
                    candidate.setcDissenting(null);
                    long sum = candidate.getcSum();
                    candidate.setcSum(sum + 1);
                    if (candidateService.updateByPrimaryKeySelective(candidate) == 1) {
                        logger.info("投票人:" + vid + "成功投票给候选人" + cid);
                    } else {
                        logger.warn("数据库执行投票人:" + vid + "给候选人" + cid + "投票失败");
                        ++count;
                    }
                    break;
            }
        }
        //验证加票有没有成功
        if (count == 0) {
            logger.info("投票人" + voter.getvName() + "成功完成投票!");
            //将最新的消息通知已经登录的投票端小伙伴
            //webSocket.sendMessage("投票人" + voter.getvName() + "成功完成投票");
            return "{\"msg\":\"成功完成投票\"}";
        } else {
            System.out.println(LogAspectUtil.LOG_SQLEXE_EORROR);
            logger.warn("数据库执行候选人加票失败");
            return "{\"msg\":\"数据库执行失败\"}";
        }

    }



    //参与投票的投票人提交评分结果（提交结果给管理员的最新的投票事件）
    @RequestMapping(value = "subMarkResult",method = RequestMethod.POST)
    public String subMarkResult(@RequestBody String str) {
        logger.info("参与评分的评分人提交结果:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"vid","mode","arr"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String vid = (String) map.get("vid");
        String mode = (String) map.get("mode");
        String cs = JSON.toJSONString(map.get("arr"));

        Voter voter = voterService.selectByPriKey(vid);
        //判断该投票人是否合法
        if (!(isRule(voter).equals(""))) {
            return isRule(voter);
        }

        //判断该评分是否合法
        if ((!(mode.equals("00"))) && (!(mode.equals("01"))) && (!(mode.equals("02")))) {
            logger.warn("评分模式" + mode + "不存在");
            return "{\"msg\":\"评分模式不存在\"}";
        }


        List<Mark> marks = JSON.parseArray(cs, Mark.class);

        //判断所评的人数是否合法
        if (voter.getvStatus() != marks.size()) {
            logger.warn("评分人" + vid + "所评的人数是不合法");
            return "{\"msg\":\"您还未完成对所有候选人的评分\"}";
        }
        //判断候选人id与所评分是否合法
        for (Mark mark : marks) {
            //检验arr中是否有null
            if (mark == null) {
                logger.warn("评分人" + vid + "提交结果含有空的mark对象");
                return "{\"msg\":\"提交结果含有不在要求范围内的分值\"}";
            } else { //判断候选人id与所评分是否合法
                //虽确定mark非null了,但是mark其中得属性全为null也有可能,还要判断属性值非null
                //判断候选人id是否合法
                Integer cid = mark.getcId();
                if (cid == null) {
                    logger.warn("评分人" + vid + "所评候选人的id不存在");
                    return "{\"msg\":\"提交结果含有不存在的候选人\"}";
                }
                //判断候选人分值是否合法
                String s = mark.getcScore();
                if ((s == null) || !(Pattern.compile("[0-9]*").matcher(s).matches()) || (s.equals(""))) {
                    logger.warn("评分人" + vid + "所评候选人" + cid + "的分值不在要求范围");
                    return "{\"msg\":\"有分值不在要求范围\"}";
                }
                int score = Integer.parseInt(s);
                switch (mode) {
                    case "00":
                        if ((score < 0) || (score > 100)) {
                            logger.warn("评分人" + vid + "所评候选人" + cid + "的分值不在要求范围");
                            return "{\"msg\":\"有分值不在要求范围\"}";
                        }
                        break;
                    case "01":
                        if ((score < 60) || (score > 100)) {
                            logger.warn("评分人" + vid + "所评候选人" + cid + "的分值不在要求范围");
                            return "{\"msg\":\"有分值不在要求范围\"}";
                        }
                        break;
                    default:
                        if ((score < 80) || (score > 100)) {
                            logger.warn("评分人" + vid + "所评候选人" + cid + "的分值不在要求范围");
                            return "{\"msg\":\"有分值不在要求范围\"}";
                        }
                        break;
                }
            }
        }

        //开始正常的业务...
        //将投票人的票数置为零
        voter.setvMid(null);
        voter.setvName(null);
        voter.setvPassword(null);
        int statusNum = voter.getvStatus();
        voter.setvStatus(0);
        if (voterService.updateByPrimaryKeySelective(voter) == 1) {
            logger.info("评分人:" + vid + "开始评分了！！！");
        } else {
            logger.warn("数据库执行评分人减票失败");
            return "{\"msg\":\"数据库执行失败\"}";
        }

        //给候选人加分,分为0-100(00)与60-100(01)与80-100(02)两种
        //记录设置成功的个数
        int count = 0;
        for (Mark mark:
                marks) {
            long cid = Long.parseLong(Integer.toString(mark.getcId()));
            String s = mark.getcScore();
            Long score = Long.valueOf(s);
            Candidate candidate = candidateService.selectByPriKey(cid);
            candidate.setcInfo(null);
            candidate.setcMid(null);
            score = score + candidate.getcSum();
            candidate.setcSum(score);
            if (candidateService.updateByPrimaryKeySelective(candidate) == 1) {
                logger.warn("评分人" + vid + "成功给候选人" + cid + "评分");
                ++count;
            } else {
                if (count > 0) {
                    System.out.println(LogAspectUtil.LOG_SQLEXE_EORROR);
                    logger.warn("数据库执行候选人加分失败,数据库操做出现异常,请检查数据库！");
                    return "{\"msg\":\"数据库执行失败\"}";
                } else {
                    logger.warn("数据库执行候选人加分失败,系统开始给投票人补尝票数");
                    voter.setvStatus(statusNum);
                    if (voterService.updateByPrimaryKeySelective(voter) == 1) {
                        logger.warn("给评分人:" + vid + "补尝票数成功");
                    } else {
                        System.out.println(LogAspectUtil.LOG_SQLEXE_EORROR);
                        logger.warn("数据库执行评分人置零失败,数据库操做连续出现异常,请检查数据库！");
                    }
                    return "{\"msg\":\"数据库执行失败\"}";
                }
            }
        }

        if (count == marks.size()) {
            logger.info("评分人" + voter.getvName() + "成功完成评分");
            //将最新的消息通知已经登录的投票端小伙伴
            //webSocket.sendMessage("评分人" + voter.getvName() + "成功完成评分");
            return "{\"msg\":\"成功完成评分\"}";
        } else {
            System.out.println(LogAspectUtil.LOG_SQLEXE_EORROR);
            logger.warn("数据库执行候选人加分失败");
            return "{\"msg\":\"数据库执行失败\"}";
        }
    }



    //管理员获取(最新的)投票结果列表
    //这里是取出当前存在于候选人表的所有的mid为传过来的mid的所有候选人,降序排列
    @RequestMapping(value = "getResults",method = RequestMethod.POST)
    public String getResults(@RequestBody String str) {
        logger.info("管理员获取投票结果列表:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"mid"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String mid = (String) map.get("mid");

        //判断该管理员是否合法
        Manager manager = managerService.selectByPriKey(mid);
        if (manager == null) {
            logger.warn("管理员" + mid + "不存在}");
            return "{\"msg\":\"该管理员不存在\"}";
        }

        //根据mid当前获取当前最新vcid
        String vcid;
        int num = 1;
        while (true) {
            String mid_num = mid + "_" + Integer.toString(num);
            //查询数据库
            Vote vote = voteService.selectByPriKey(mid_num);
            if (vote == null) {
                break;
            }
            num++;
        }
        if ((num -1) != 0) {
            vcid = mid + "_" + Integer.toString((num -1));
        } else {
            logger.info("管理员" + mid + "还没有发起任何投票");
            return "{\"msg\":\"还没有发起任何投票\"}";
        }

        //查询投票模式
        Vote vote = voteService.selectByPriKey(vcid);
        String mode = vote.getVcMode();

        //通用Example进行查询
        Example example = new Example(Candidate.class);
        //selectProperties一定要放在第一个才能生效
        example.selectProperties("cId","cInfo","cSum","cAbstention","cDissenting");
        example.createCriteria()
                .andEqualTo("cMid",mid)
                .andEqualTo("cVcid",vcid);
        example.orderBy("cSum")
                .desc()
                .orderBy("cDissenting")
                .orderBy("cAbstention")
                .asc();
        List<Candidate> candidates = candidateService.selectByExample(example);
        logger.info("candidate原生对象list:" + candidates);

        /*
        处理复杂数据类型的拼装开始
        这里的拼装和CandidateController类的getCandidates方法里的拼装几乎一样
        建议抽成一个工具类
         */
        //重新拼装candidates,拼装成简单类型的json对象数组
        StringBuilder cs = new StringBuilder("[");
        for (Candidate candidate:
                candidates) {
            //使用LinkedHashMap解决顺序问题、使用TypeReference指明反序列化的类型
            TypeReference<LinkedHashMap<String, String>> typeReference = new TypeReference<LinkedHashMap<String, String>>() {
            };
            LinkedHashMap<String,String> hashMap = JSON.parseObject(candidate.getcInfo(), typeReference);
            String csum = String.valueOf(candidate.getcSum());
            //判断是投票还是评分
            if (!(mode.equals("11"))) {
                hashMap.put("分数",csum);
            } else {
                hashMap.put("赞同",csum);
                hashMap.put("弃权",Integer.toString(candidate.getcAbstention()));
                hashMap.put("反对",Integer.toString(candidate.getcDissenting()));
            }
            String cid = String.valueOf(candidate.getcId());
            hashMap.put("cId",cid);
            //System.out.println("hrs" + hashMap);
            String info = JSON.toJSONString(hashMap);
            //logger.info("Json后的字符串" + info);
            cs.append(info).append(",");
        }
        //去除最后一个逗号
        int end = cs.length();
        cs = new StringBuilder(cs.substring(0, (end - 1)));
        cs.append("]");
        logger.info("拼装后的字符串" + cs);
        /*
        处理复杂数据类型的拼装结束
         */

        logger.info("{\"msg\":\"111\",\"vcid\":\"" + vcid + "\",\"mode\":\"" + mode + "\",\"info\":" + cs + "}");
        return "{\"msg\":\"111\",\"vcid\":\"" + vcid + "\",\"mode\":\"" + mode + "\",\"info\":" + cs + "}";
    }



    //管理员结束本次投票(谨慎调用该方法,该方法会清空调用该方法得管理员得所有候选人信息、所有投票信息,只保留投票人表)
    @RequestMapping(value = "endVote",method = RequestMethod.POST)
    public String endVote(@RequestBody String str) {
        logger.info("管理员结束本次投票:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"mid","pwd"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String mid = (String) map.get("mid");
        String pwd = (String) map.get("pwd");

        return clearVote(mid, pwd);
    }



    @NotNull//非空校验
    //判断该投票是否合法
    private String isRule(Voter voter) {
        if (voter == null) {
            logger.warn("投票人不存在");
            return "{\"msg\":\"投票人不存在\"}";
        }
        if (managerService.selectByPriKey(voter.getvMid()) == null) {
            logger.warn("投票人的管理员" + voter.getvMid() + "不存在");
            return "{\"msg\":\"投票人的管理员不存在\"}";
        }
        if (voter.getvStatus() == 0) {
            logger.warn("投票人没有可用投票次数");
            return "{\"msg\":\"您没有可用投票次数\"}";
        }
        return "";
    }



    //删除最近一次投票的所有相关信息(投票人信息与管理员账号密码除除外)
    @NotNull
    private String clearVote(String mid, String pwd) {
        //判断该管理员是否存在
        Manager manager = managerService.selectByPriKey(mid);
        if (manager == null) {
            logger.warn("管理员" + mid + "不存在");
            return "{\"msg\":\"该管理员不存在\"}";
        } else if (!(pwd.equals(manager.getmPassword()))){
            logger.warn("管理员" + mid + "的密码错误");
            return "{\"msg\":\"密码错误\"}";
        }

        //删除所有候选人
        //通用Example进行查询
        Example candidateExample = new Example(Candidate.class);
        candidateExample.createCriteria().andEqualTo("cMid",mid);
        Integer cnum = candidateService.deleteByExample(candidateExample);
        logger.info("成功删除" + cnum + "条候选人");

        //删除所有投票记录
        //通用Example进行查询
        Example voteExample = new Example(Vote.class);
        voteExample.createCriteria().andEqualTo("vcMid",mid);
        Integer vcnum = voteService.deleteByExample(voteExample);
        logger.info("成功删除" + vcnum + "条投票记录");

        //删除管理员身上的所有属性信息
        manager.setmFiles(null);
        manager.setmFields(null);
        if (managerService.updateByPrimaryKey(manager) == 1) {
            logger.info("成功删除管理员的" + mid + "所有属性信息");
        } else {
            logger.warn("数据库执行删除管理员" + mid + "的所有属性信息失败");
        }

        //置零管理员旗下所有投票人的状态信息
        //两种思路：我们出于干净考虑用第二种思路
        //       1、直接把所有存在的投票涉及的投票人状态清零
        //       2、把管理员旗下所有投票人状态信息不为零的记录全部置零
        //通用Example进行查询
        Example voterExample = new Example(Voter.class);
        voterExample.createCriteria().andEqualTo("vMid",mid).andNotEqualTo("vStatus",0);
        Voter voter = new Voter(0);
        Integer vnum = voterService.updateByExampleSelective(voter, voterExample);
        logger.info("成功置零" + vnum + "条投票人状态信息");

        return "{\"msg\":\"111\"}";
    }



}
