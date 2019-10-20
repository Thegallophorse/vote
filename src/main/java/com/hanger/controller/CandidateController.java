package com.hanger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hanger.entity.Candidate;
import com.hanger.entity.Manager;
import com.hanger.entity.Vote;
import com.hanger.service.CandidateService;
import com.hanger.service.ManagerService;
import com.hanger.service.VoteService;
import com.hanger.util.JsonUtil;
import com.hanger.util.StrArrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;


@RestController
@CrossOrigin
public class CandidateController {
    private Logger logger = LoggerFactory.getLogger(CandidateController.class);

    private final ManagerService managerService;
    private final CandidateService candidateService;
    private final VoteService voteService;

    public CandidateController(ManagerService managerService, CandidateService candidateService, VoteService voteService) {
        this.managerService = managerService;
        this.candidateService = candidateService;
        this.voteService = voteService;
    }


    //添加单个候选人
    @RequestMapping(value = "insertCandidate",method = RequestMethod.POST)
    public String insertCandidate(@RequestBody String str) {
        logger.info("添加单个候选人" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);

        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"id","vs"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String mid = (String) map.get("id");
        String vs = (String) map.get("vs");

        //判断该管理员是否存在
        Manager manager = managerService.selectByPriKey(mid);
        if (manager == null) {
            logger.warn("{\"msg\":\"该管理员不存在\"}");
            return "{\"msg\":\"该管理员不存在\"}";
        }

        //判断该管理员是否设置了候选人的属性（文件字段可有可无属性字段必须有）
        if (manager.getmFields() == null) {
            logger.warn("{\"msg\":\"请先设置候选人的属性\"}");
            return "{\"msg\":\"请先设置候选人的属性\"}";
        }

        Example example = new Example(Vote.class);
        example.createCriteria().andEqualTo("vcMid",mid);
        List<Vote> votes = voteService.selectByExample(example);
        //logger.info("对象list集合" + voters);
        if (votes.size() != 0) {
            logger.warn("{\"msg\":\"候选人有活动正在进行\"}");
            return "{\"msg\":\"候选人有活动正在进行\"}";
        }

        String [] values = StrArrUtil.stringToArray(vs);
        //判断该值组是否可用
        if ((values == null) || (values.length == 0)) {
            logger.warn("值组不可用");
            return "{\"msg\":\"值组不可用\"}";
        }
        logger.info( "候选人值信息:");
        for (String s:
                values ) {
            logger.info(s);
        }

        String fields = manager.getmFields();
        String files = manager.getmFiles();
        HashMap keyMap = JSON.parseObject(fields , HashMap.class);
        HashMap fsMap = JSON.parseObject(files , HashMap.class);
        logger.info("应该有的属性组" + keyMap + "文件属性组" + fsMap);

        int size = keyMap.size();
        if (files != null) {
            size = size + fsMap.size();
        }

        if (size != values.length) {
            return "{\"msg\":\"000\"}";
        } else {
            LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
            Candidate candidate = new Candidate();
            for (int num = 0;num < keyMap.size(); num++) {
                String snum = Integer.toString(num);
                String fs = (String) keyMap.get(snum);
                hashMap.put(fs,values[num]);
                //System.out.println(num + "values" + values[num]);
                //System.out.println(num + "fields" + fs);
            }
            //此处大于与导入候选人表的不等于作用一样！为了不报重复代码暂时这样写
            if (size > keyMap.size()) {
                for (int num = 0;num < fsMap.size(); num++) {
                    String snum = Integer.toString(num);
                    String fs = (String) fsMap.get(snum);
                    hashMap.put(fs,values[num + keyMap.size()]);
                }
            }

            //System.out.println("hrs" + hashMap);
            String cInfo = JSON.toJSONString(hashMap);
            //System.out.println(cInfo);

            candidate.setcInfo(cInfo);
            candidate.setcMid(mid);
            if (candidateService.insertSelective(candidate) == 1) {
                //这就是通用mapper的回显功能！回显主键
                logger.info("数据库生成的唯一id：" + candidate.getcId());
                logger.info("管理员:" + mid + "成功添加新候选人" + candidate.getcId());
                return "{\"msg\":\"111\"}";
            } else {
                logger.info("管理员:" + mid + "添加新候选人" + candidate.getcId() + "失败");
                return "{\"msg\":\"数据库执行失败\"}";
            }

        }
    }



    //修改候选人
    @RequestMapping(value = "alterCandidate",method = RequestMethod.POST)
    public String alterCandidate(@RequestBody String str) {
        logger.info("修改候选人" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"mid","fs","vs"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String mid = (String) map.get("mid");
        String fs = JSON.toJSONString(map.get("fs"));
        String vs = JSON.toJSONString(map.get("vs"));

        //判断该管理员是否存在
        Manager manager = managerService.selectByPriKey(mid);
        if (manager == null) {
            logger.warn("{\"msg\":\"该管理员不存在\"}");
            return "{\"msg\":\"该管理员不存在\"}";
        }

        //判断该管理员是否设置了候选人的属性（文件字段可有可无属性字段必须有）
        if (manager.getmFields() == null) {
            logger.warn("{\"msg\":\"请先设置候选人的属性\"}");
            return "{\"msg\":\"请先设置候选人的属性\"}";
        }

        Example example = new Example(Vote.class);
        example.createCriteria().andEqualTo("vcMid",mid);
        List<Vote> votes = voteService.selectByExample(example);
        //logger.info("对象list集合" + voters);
        if (votes.size() != 0) {
            logger.warn("{\"msg\":\"候选人有活动正在进行\"}");
            return "{\"msg\":\"候选人有活动正在进行\"}";
        }

        String[] fields = StrArrUtil.stringToArray(fs);
        //判断该属性组是否可用
        if ((fields == null) || (fields.length == 0)) {
            logger.warn("属性组不可用");
            return "{\"msg\":\"属性组不可用\"}";
        }
//        logger.info("候选人属性信息:");
//        for (String s :
//                fields) {
//            System.out.println(s);
//        }
        String[] values = StrArrUtil.stringToArray(vs);
        //判断该值组是否可用
        if ((values == null) || (values.length == 0)) {
            logger.warn("值组不可用");
            return "{\"msg\":\"值组不可用\"}";
        }
//        logger.info("候选人值信息:");
//        for (String s :
//                values) {
//            System.out.println(s);
//        }

        /*fs[...]属性字符数组里面必须包含cId属性
        Integer.parseInt(fields[i]);
         */
        long id = -1;
        for (int i = 0;i < fields.length;i++) {
            if (fields[i].equals("cId")) {
                id = Long.parseLong(values[i]);
            }
        }
        //判断是否包含cId属性
        if (id == -1) {
            logger.warn("缺少候选人id参数:cId");
            return "{\"msg\":\"缺少候选人id参数:cId\"}";
        }
        Candidate candidate = candidateService.selectByPriKey(id);
        //判断该候选人是否存在
        if (candidate == null) {
            logger.warn("该候选人不存在");
            return "{\"msg\":\"该候选人不存在\"}";
        }
        logger.info("候选人原来的信息" + candidate);

        //判断传过来的属性个数与值的个数是否相等
        if (fields.length != values.length) {
            return "{\"msg\":\"000\"}";
        } else {
            LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
            Candidate trueCandidate = new Candidate();
            //判断为了舍弃最后一个cId
            for (int num = 0;num < fields.length; num++) {
                if (!(fields[num].equals("cId"))) {
                    hashMap.put(fields[num],values[num]);
                    //System.out.println(num + "fields" + fields[num]);
                    //System.out.println(num + "values" + values[num]);
                }
            }
            //System.out.println("hrs" + hashMap);
            String cInfo = JSON.toJSONString(hashMap);
            //System.out.println(cInfo);

            trueCandidate.setcInfo(cInfo);
            trueCandidate.setcId(id);

            //通用mapper只要调用就会执行,执行成功返回整型1
            if (candidateService.updateByPrimaryKeySelective(trueCandidate) == 1){
                logger.info("管理员:" + mid + "成功修改候选人" + id);
                return "{\"msg\":\"111\"}";
            } else {
                return "{\"msg\":\"数据库执行失败\"}";
            }
        }
    }



    //删除已有候选人
    @RequestMapping(value = "deleteCandidates",method = RequestMethod.POST)
    public String deleteCandidates(@RequestBody String str) {
        logger.info("del候选人:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"id","arr"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String mid = (String) map.get("id");
        String ids = JSON.toJSONString(map.get("arr"));
        //判断该管理员是否存在
        Manager manager = managerService.selectByPriKey(mid);
        if (manager == null) {
            logger.warn("{\"msg\":\"该管理员不存在\"}");
            return "{\"msg\":\"该管理员不存在\"}";
        }

        //判断该管理员是否设置了候选人的属性（文件字段可有可无属性字段必须有）
        if (manager.getmFields() == null) {
            logger.warn("{\"msg\":\"请先设置候选人的属性\"}");
            return "{\"msg\":\"请先设置候选人的属性\"}";
        }

        Example example = new Example(Vote.class);
        example.createCriteria().andEqualTo("vcMid",mid);
        List<Vote> votes = voteService.selectByExample(example);
        //logger.info("对象list集合" + voters);
        if (votes.size() != 0) {
            logger.warn("{\"msg\":\"候选人有活动正在进行\"}");
            return "{\"msg\":\"候选人有活动正在进行\"}";
        }

        String[] cids = StrArrUtil.stringToArray(ids);
        //判断该属性组是否可用
        if ((cids == null) || (cids.length == 0)) {
            logger.warn("候选人id组arr为空");
            return "{\"msg\":\"候选人id组不可用\"}";
        }

        //存储每个id执行后的状态码code
        String [] codes = new String[cids.length];
        //记录删除成功的个数
        int count = 0;
        logger.info("候选人id信息:");
        for (int i = 0;i < cids.length;i++) {
            String s = cids[i];
            logger.info("删除候选人:" + s);
            long id = Long.parseLong(s);
            //通用mapper只要调用就会执行,执行成功返回执行成功的个数(这里是1)
            if (candidateService.deleteByPrimaryKey(id) == 1){
                logger.info("管理员:" + mid + "成功删除候选人" + id);
                codes[i] = "111";
                ++count;
            } else {
                logger.info("删除失败");
                codes[i] = "000";
            }
        }

        logger.info("共:" + cids.length + "条,删除成功:" + count + "条,删除失败:" + (cids.length - count) + "条");
        String msg = JSON.toJSONString(codes);
        return "{\"msg\":" + msg + "}";
    }



    //获取已有候选人的分页数据
    @RequestMapping(value = "getCandidates",method = RequestMethod.POST)
    public String getCandidates(@RequestBody String str) {
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
        Manager manager = managerService.selectByPriKey(id);
        if (manager == null) {
            logger.warn("该管理员不存在");
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

        logger.info("获取管理员：" + id + "的第" + pn + "页候选人");
        //Mapper接口方式的调用PageHelper（推荐）
        // 只有紧跟在PageHelper.startPage方法后的第一个Mybatis的查询（Select）方法会被分页
        PageHelper.startPage(pn, 20);

        //通用Example进行查询
        Example example = new Example(Candidate.class);
        example.createCriteria().andEqualTo("cMid",id);
        List<Candidate> candidates = candidateService.selectByExample(example);
        //logger.info("对象list集合" + voters);

        //重新拼装candidates
        StringBuilder cs = new StringBuilder("[");
        for (Candidate candidate:
             candidates) {
            //使用LinkedHashMap解决顺序问题、使用TypeReference指明反序列化的类型
            TypeReference<LinkedHashMap<String, String>> typeReference = new TypeReference<LinkedHashMap<String, String>>() {
            };
            LinkedHashMap<String,String> hashMap = JSON.parseObject(candidate.getcInfo(), typeReference);
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

        //用PageInfo对结果进行包装
        PageInfo<Candidate> info = new PageInfo<>(candidates);
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

        //获取文件属性的个数
        int filenum = 0;
        String files = manager.getmFiles();
        if (files != null) {
            HashMap fsMap = JSON.parseObject(files , HashMap.class);
            filenum = fsMap.size();
            logger.info("文件属性有" + filenum + "个");
        } else {
            logger.info("文件属性有" + filenum + "个");
        }

        String tp = Integer.toString(info.getPages());
        String td = Long.toString(info.getTotal());

        logger.info("{\"msg\":\"111\",\"tp\":" + tp + ",\"td\":" + td + ",\"info\":" + cs + "}");
        return "{\"msg\":\"111\",\"tp\":" + tp + ",\"td\":" + td +  ",\"flen\":" + filenum + ",\"info\":" + cs + "}";

    }



}
