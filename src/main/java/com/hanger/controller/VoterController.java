package com.hanger.controller;


import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hanger.entity.Voter;
import com.hanger.service.ManagerService;
import com.hanger.service.VoterService;
import com.hanger.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;



@RestController
@CrossOrigin
public class VoterController {
    private Logger logger = LoggerFactory.getLogger(VoterController.class);

    private final ManagerService managerService;
    private final VoterService voterService;

    @Autowired
    public VoterController(ManagerService managerService, VoterService voterService) {
        this.managerService = managerService;
        this.voterService = voterService;
    }



    //添加单个投票人
    @RequestMapping(value = "insertVoter",method = RequestMethod.POST)
    public String insertVoter(@RequestBody String str) {
        logger.info("添加单个投票人:" + str);

        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"vId","vPassword","vName","vMid"};
        if (!(JsonUtil.checkFormat(str , jsonKeys , 5))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }
        Voter voter = JSON.parseObject(str, Voter.class);
        logger.info( "投票人信息:" + voter);
        String id = voter.getvId();
        String pwd = voter.getvPassword();
        String name = voter.getvName();
        String mid = voter.getvMid();

        if (managerService.selectByPriKey(mid) == null) {
            logger.warn("未找到该管理员:" + mid);
            return "{\"msg\":\"未找到该管理员\",\"code\":\"110\"}";
        } else if (voterService.selectByPriKey(id) != null) {
            logger.warn("用户ID已存在");
            return "{\"msg\":\"用户ID已存在\",\"code\":\"001\"}";
        } else if (!(Pattern.compile("[A-Za-z0-9]{4,8}").matcher(id).matches())) {
            logger.warn("用户ID格式错误");
            return "{\"msg\":\"用户ID格式错误\",\"code\":\"000\"}";
        } else if (!(Pattern.compile("[A-Za-z0-9]{4,8}").matcher(pwd).matches())) {
            logger.warn("密码格式错误");
            return "{\"msg\":\"密码格式错误\",\"code\":\"010\"}";
        } else if ((name.length() <= 0) || (name.length() > 100)) {
            logger.warn("姓名格式错误");
            return "{\"msg\":\"姓名格式错误\",\"code\":\"100\"}";
        } else {
            if (voterService.insertSelective(voter) == 1) {
                logger.info("管理员:" + voter.getvMid() + "成功添加新用户" + id);
                return "{\"msg\":\"添加成功\",\"code\":\"111\"}";
            } else {
                logger.warn("管理员:" + voter.getvMid() + "添加新用户" + id + "失败");
                return "{\"msg\":\"数据库执行失败\"}";
            }
        }
    }



    //修改投票人
    @RequestMapping(value = "alterVoter",method = RequestMethod.POST)
    public String alterVoter(@RequestBody String str) {
        logger.info("修改投票人:" + str);

        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"vId","vPassword","vName","vMid"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String id = (String) map.get("vId");
        String pwd = (String) map.get("vPassword");
        String name = (String) map.get("vName");
        String mid = (String) map.get("vMid");

        Voter trueVoter = voterService.selectByPriKey(id);
        logger.info( "投票人原始信息:" + trueVoter);
        if ((trueVoter == null) || (!(trueVoter.getvMid().equals(mid)))) {
            logger.warn("未找到该投票人");
            return "{\"msg\":\"未找到该投票人\",\"code\":\"000\"}";
        } else if (trueVoter.getvStatus() != 0) {
            logger.warn("该投票人正在参与投票");
            return "{\"msg\":\"该投票人正在参与投票\",\"code\":\"000\"}";
        } else if (managerService.selectByPriKey(mid) == null) {
            logger.warn("未找到该管理员");
            return "{\"msg\":\"未找到该管理员\",\"code\":\"110\"}";
        } else if (!(Pattern.compile("[A-Za-z0-9]{4,8}").matcher(pwd).matches())) {
            logger.warn("密码格式错误");
            return "{\"msg\":\"密码格式错误\",\"code\":\"010\"}";
        } else if ((name.length() <= 0) || (name.length() >= 100)) {
            logger.warn("姓名格式错误");
            return "{\"msg\":\"姓名格式错误\",\"code\":\"100\"}";
        } else {
            if (name.equals(trueVoter.getvName()) ) {
                trueVoter.setvName(null);
                if (pwd.equals(trueVoter.getvPassword())) {
                    return "{\"msg\":\"修改成功\",\"code\":\"111\"}";
                } else {
                    trueVoter.setvPassword(pwd);
                }
            } else {
                trueVoter.setvName(name);
                if (pwd.equals(trueVoter.getvPassword())) {
                    trueVoter.setvPassword(null);
                } else {
                    trueVoter.setvPassword(pwd);
                }
            }

            /*
            updateByPrimaryKeySelective(trueVoter)
            修改传入的对象的存在的属性，如果为null就不修改，但是主键一定不能为null
            而且除了主键外的其他键也不能全为null
            //trueVoter.setvId(null);
             */
            //这里直接把管理员id(vMid)置null,就算修改了也没有用
            trueVoter.setvMid(null);
            trueVoter.setvStatus(null);
            logger.info( "要修改的信息(id不变):" + trueVoter);
            //通用mapper只要调用就会执行,执行成功返回执行成功的个数(这里是1)
            if (voterService.updateByPrimaryKeySelective(trueVoter) == 1){
                logger.info("管理员:" + mid + "成功修改用户" + id);
                return "{\"msg\":\"修改成功\",\"code\":\"111\"}";
            }else {
                return "{\"msg\":\"数据库执行失败\",\"code\":\"1111\"}";
            }
        }
    }



    //删除已有投票人
    @RequestMapping(value = "deleteVoter",method = RequestMethod.POST)
    public String deleteVoter(@RequestBody String str) {
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"id","arr"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String mid = (String) map.get("id");
        String arr = (String) map.get("arr");

        //判断该管理员是否存在
        if (managerService.selectByPriKey(mid) == null) {
            logger.warn("该管理员不存在");
            return "{\"msg\":\"该管理员不存在\"}";
        }

        List<Voter> voters = JSON.parseArray(arr, Voter.class);
        logger.info("管理员:" + mid + "删除已有投票人" + voters);

        //存储每个id执行后的状态码code
        String [] codes = new String[voters.size()];
        //记录删除成功的个数
        int count = 0;
        for (int i =0; i < voters.size(); i++) {
            Voter voter = voters.get(i);
            String id = voter.getvId();
            String pwd = voter.getvPassword();
            String name = voter.getvName();
            //找到数据库的Voter
            Voter trueVoter = voterService.selectByPriKey(id);
            if (trueVoter == null) {
                logger.info("管理员:" + mid + "删除的投票人" + id + "不存在");
                codes[i] = "000";
            } else if (trueVoter.getvStatus() != 0) {
                logger.info("管理员:" + mid + "删除的投票人" + id + "正在参与投票");
                codes[i] = "001";
            } else if (!(mid.equals(trueVoter.getvMid()))) {
                logger.info("管理员:" + mid + "删除的投票人" + id + "不属于自己");
                codes[i] = "110";
            } else if (!(name.equals(trueVoter.getvName()))) {
                logger.info("管理员:" + mid + "删除的投票人" + id + "的名字校验错误");
                codes[i] = "100";
            } else if (!(pwd.equals(trueVoter.getvPassword()))) {
                logger.info("管理员:" + mid + "删除的投票人" + id + "的密码校验错误");
                codes[i] = "010";
            } else {
                if (voterService.deleteByPriKey(id) == 1) {
                    logger.info("管理员:" + mid + "成功删除用户" + id);
                    codes[i] = "111";
                    ++count;
                } else {
                    logger.info("数据库执行管理员:" + mid + "删除用户" + id + "失败");
                }
            }
        }

        logger.info("共:" + voters.size() + "条,删除成功:" + count + "条,删除失败:" + (voters.size() - count) + "条");
        String msg = JSON.toJSONString(codes);
        return "{\"msg\":" + msg + "}";
    }



    //获取已有投票人的分页数据
    @RequestMapping(value = "getVoters",method = RequestMethod.POST)
    public String getVoters(@RequestBody String str) {
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
            logger.warn("该管理员不存在");
            return "{\"msg\":\"该管理员不存在\"}";
        }
        //判断页码是否合法
        if (!(Pattern.compile("[0-9]*").matcher(spn).matches()) || (spn.equals(""))){
            logger.warn("该页码不存在");
            return "{\"msg\":\"该页码不存在\"}";
        }
        int pn = Integer.parseInt(spn);
        if (pn <= 0){
            logger.warn("该页码不存在");
            return "{\"msg\":\"该页码不存在\"}";
        }

        logger.info("获取管理员：" + id + "的第" + pn + "页投票人");
        //Mapper接口方式的调用PageHelper（推荐）
        // 只有紧跟在PageHelper.startPage方法后的第一个Mybatis的查询（Select）方法会被分页
        PageHelper.startPage(pn, 20);

        //通用Example进行查询
        Example example = new Example(Voter.class);
        example.createCriteria().andEqualTo("vMid",id);
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



    //投票人获取自己的个人信息
    @RequestMapping(value = "getMyInfo",method = RequestMethod.POST)
    public String getMyInfo(@RequestBody String str) {
        logger.info("发过来的信息：" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"id"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String id = (String) map.get("id");

        Voter voter = voterService.selectByPriKey(id);
        //判断该投票人是否存在
        if (voter == null) {
            logger.warn("该投票人不存在");
            return "{\"msg\":\"该投票人不存在\"}";
        }

        logger.info("原生voter对象" + voter);
        String vs = JSON.toJSONString(voter);
        logger.info("Json后的字符串" + vs);

        logger.info("{\"msg\":\"111\",\"info\":" + vs + "}");
        return "{\"msg\":\"111\",\"info\":" + vs + "}";
    }







}


