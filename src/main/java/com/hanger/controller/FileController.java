package com.hanger.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.hanger.entity.Candidate;
import com.hanger.entity.Manager;
import com.hanger.entity.Vote;
import com.hanger.entity.Voter;
import com.hanger.service.CandidateService;
import com.hanger.service.ManagerService;
import com.hanger.service.VoteService;
import com.hanger.service.VoterService;
import com.hanger.util.ExcelUtil;
import com.hanger.util.FileUtil;
import com.hanger.util.JsonUtil;
import org.jodconverter.DocumentConverter;
import org.jodconverter.office.OfficeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;


@RestController
@CrossOrigin
public class FileController {
    private Logger logger = LoggerFactory.getLogger(FileController.class);

    private final VoteService voteService;
    private final CandidateService candidateService;
    private final ManagerService managerService;
    private final VoterService voterService;
    private final DocumentConverter documentConverter;

    @Autowired
    public FileController(VoteService voteService, CandidateService candidateService, ManagerService managerService, VoterService voterService, @Qualifier("jodConverter") DocumentConverter documentConverter) {
        this.voteService = voteService;
        this.candidateService = candidateService;
        this.managerService = managerService;
        this.voterService = voterService;
        this.documentConverter = documentConverter;
    }





    //管理员导出投票人信息的excel表模板
    @RequestMapping(value = "excelVotersTemplate", method = RequestMethod.POST)
    public String excelVoter(@RequestBody String str) {
        logger.info("管理员导出投票人信息的excel表模板:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"mid"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String mid = (String) map.get("mid");

        //判断管理员是否存在
        Manager manager = managerService.selectByPriKey(mid);
        if (manager == null) {
            logger.warn("未找到管理员" + mid);
            return "{\"msg\":\"未找到管理员\"}";
        }

        String [] fields = {"用户ID","密码","姓名"};

        //windows
        String path = "C://Logging//vote1.0//files//manager//" + manager.getmId() + "//";
        //linux
        //String path = "/Logging/vote1.0/files/manager/" + manager.getmId() + "/";
        String name = "投票人信息导入模板";

        if (ExcelUtil.createExcel(path, name, fields)) {
            logger.info("生成成功！");
            return "{\"msg\":\"111\",\"url\":\"" + path + name + "\"}";
        } else {
            return "{\"msg\":\"生成excel失败\"}";
        }

    }



    //提交表单导入excel的方式添加投票人
    @RequestMapping(value = "excelVoters" ,method = RequestMethod.POST)
    public String excelVoter(@RequestParam("file") MultipartFile file , @RequestParam("mid") String mid) {
        //判断该管理员是否存在
        if (managerService.selectByPriKey(mid) == null) {
            logger.warn("该管理员不存在");
            return "{\"msg\":\"该管理员不存在\"}";
        }

        //判断该管理员上传文件是否为空
        if (file == null) {
            logger.warn("管理员" + mid + "上传文件为空");
            return "{\"msg\":\"上传文件为空\"}";
        }

        logger.info("管理员:" + mid + "使用excel表添加投票人");

        try {
            List<String[]> list = ExcelUtil.readExcel(file);
            //判断excel文件是否可用
            if (list.get(0)[0].equals("失败！")){
                logger.info("excel文件错误");
                return "{\"msg\":\"excel文件错误\"}";
            }
            //存储每个id执行后的状态码code
            String [] codes = new String[list.size()];
            //记录插入成功的个数
            int count = 0;
            for(int i = 0;i<list.size();i++) {
                String[] voters = list.get(i);

                Voter voter = new Voter();
                voter.setvId(voters[0]);
                voter.setvPassword(voters[1]);
                voter.setvName(voters[2]);
                voter.setvMid(mid);

                logger.info("开始添加第" + (i + 1) + "条：" + voter);
                String id = voter.getvId();
                String pwd = voter.getvPassword();
                String name = voter.getvName();
                if (voterService.selectByPriKey(id) != null) {
                    codes[i] = "001";
                } else if (!(Pattern.compile("[A-Za-z0-9]{4,8}").matcher(id).matches())) {
                    codes[i] = "000";
                } else if (!(Pattern.compile("[A-Za-z0-9]{4,8}").matcher(pwd).matches())) {
                    codes[i] = "010";
                }else  if ((name.length() <= 0) || (name.length() > 100)) {
                    codes[i] = "100";
                } else {
                    if (voterService.insertSelective(voter) == 1) {
                        logger.info("管理员:" + voter.getvMid() + "成功添加新用户" + id);
                        codes[i] = "111";
                        ++count;
                    } else {
                        logger.info("管理员:" + voter.getvMid() + "添加新用户" + id + "失败");
                    }
                }
            }

            logger.info("共:" + list.size() + "条,添加成功:" + count + "条,添加失败:" + (list.size() - count) + "条");
            String msg = JSON.toJSONString(codes);
            return "{\"msg\":" + msg + "}";

        } catch (IOException e) {
            logger.info("读取excel文件失败", e);
            return "{\"msg\":\"读取excel文件失败\"}";
        }
    }



    //管理员导出候选人信息的excel表模板
    @RequestMapping(value = "excelCandidatesTemplate", method = RequestMethod.POST)
    public String excelCandidatesTemplate(@RequestBody String str) {
        logger.info("管理员导出候选人信息的excel表模板:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"mid"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String mid = (String) map.get("mid");

        //判断管理员是否存在
        Manager manager = managerService.selectByPriKey(mid);
        if (manager == null) {
            logger.warn("未找到管理员" + mid);
            return "{\"msg\":\"未找到管理员\"}";
        }

        String fds = manager.getmFields();
        String files = manager.getmFiles();
        logger.info("查出来的原生属性：" + fds);
        logger.info("查出来的原生文件属性：" + files);
        if (fds == null) {
            logger.warn("管理员" + mid + "还未设置属性组");
            return "{\"msg\":\"管理员还未设置属性组\"}";
        }

        HashMap fdMap = JSON.parseObject(fds, HashMap.class);
        HashMap fileMap = JSON.parseObject(files, HashMap.class);
        logger.info("属性转换为hashMap后：" + fdMap);
        logger.info("文件属性转换为hashMap后：" + fileMap);

        //设置存放属性组的字符数组大小
        String[] fields = new String[fdMap.size()];
        String[] temp;
        if (files != null) {
            temp = new String[fdMap.size() + fileMap.size()];
        } else {
            temp = fields;
        }

        //存放属性组
        for (int i = 0;i < fdMap.size();i++) {
            String key = Integer.toString(i);
            fields[i] = String.valueOf(fdMap.get(key));

            temp[i] = String.valueOf(fdMap.get(key));
        }
        //判断是否有文件属性组,有则利用temp续属性列
        if (files != null) {
            //存放文件属性组
            for (int i = fdMap.size();i < fdMap.size() + fileMap.size();i++) {
                String key = Integer.toString(i - fdMap.size());
                temp[i] = String.valueOf(fileMap.get(key));
            }
            fields = temp;
        }

        //windows
        String path = "C://Logging//vote1.0//files//manager//" + manager.getmId() + "//";
        //linux
        //String path = "/Logging/vote1.0/files/manager/" + manager.getmId() + "/";
        String name = "候选人信息导入模板";

        if (ExcelUtil.createExcel(path, name, fields)) {
            logger.info("生成成功！");
            return "{\"msg\":\"111\",\"url\":\"" + path + name + "\"}";
        } else {
            return "{\"msg\":\"生成excel失败\"}";
        }

    }



    //导入excel的方式添加候选人
    @RequestMapping(value = "excelCandidates" ,method = RequestMethod.POST)
    public String excelCandidates(@RequestParam("file") MultipartFile file , @RequestParam("mid") String mid) {
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

        //判断该管理员上传文件是否为空
        if (file == null) {
            logger.warn("管理员" + mid + "上传文件为空");
            return "{\"msg\":\"上传文件为空\"}";
        }

        logger.info("管理员:" + mid + "使用excel表添加候选人");

        try {
            List<String[]> list = ExcelUtil.readExcel(file);
            //判断excel文件是否可用
            if (list.get(0)[0].equals("失败！")){
                logger.warn("excel文件错误");
                return "{\"msg\":\"excel文件错误\"}";
            }
            //存储每个id执行后的状态码code
            String [] codes = new String[list.size()];
//            System.out.println(list.size() + "条数据");
            //记录插入成功的个数
            int count = 0;

            String fields = manager.getmFields();
            String files = manager.getmFiles();
            HashMap keyMap = JSON.parseObject(fields , HashMap.class);
            HashMap fsMap = JSON.parseObject(files , HashMap.class);
            for(int i = 0;i < list.size();i++) {
                String[] cvalues = list.get(i);

                int size = keyMap.size();
                if (files != null) {
                    size = size + fsMap.size();
                }

                //判断管理员的属性组是否为空
                if (size == 0) {
                    logger.warn("管理员" + mid + "的属性组为空");
                    return "{\"msg\":\"管理员的属性组为空\"}";
                }

                if (cvalues.length != size) {
                    logger.info("表中存在某行的字段个数与管理员设置的字段长度不同");
                    codes[i] = "000";
                } else {
                    LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
                    logger.info("开始添加第" + (i + 1) + "条信息");
                    Candidate candidate = new Candidate();
                    //System.out.println(candidate.toString());

                    for (int num = 0;num < keyMap.size(); num++) {
                        String snum = Integer.toString(num);
                        String fs = (String) keyMap.get(snum);
                        hashMap.put(fs,cvalues[num]);

                        //System.out.println(num + "vs" + cvalues[num]);
                        //System.out.println(num + "fs" + fs);
                    }
                    if (size != keyMap.size()) {
                        for (int num = 0;num < fsMap.size(); num++) {
                            String snum = Integer.toString(num);
                            String fs = (String) fsMap.get(snum);
                            hashMap.put(fs,cvalues[num + keyMap.size()]);
                            //System.out.println(num + "vs" + cvalues[num]);
                            //System.out.println(num + "fs" + fs);
                        }
                    }

                    //System.out.println("hrs" + hashMap);
                    String cInfo = JSON.toJSONString(hashMap);
                    logger.info("即将添加:" + cInfo);

                    candidate.setcInfo(cInfo);
                    candidate.setcMid(mid);

                    if (candidateService.insertSelective(candidate) == 1) {
                        //这就是通用mapper的回显功能！回显主键
                        logger.info("数据库生成的唯一id：" + candidate.getcId());
                        logger.info("管理员:" + mid + "成功添加新候选人" + candidate.getcId());
                        codes[i] = "111";
                        ++count;
                    } else {
                        logger.info("管理员:" + mid + "添加新候选人" + candidate.getcId() + "失败");
                    }
                }
            }

            logger.info("共:" + list.size() + "条,添加成功:" + count + "条,添加失败:" + (list.size() - count) + "条");
            String msg = JSON.toJSONString(codes);
            return "{\"msg\":" + msg + "}";

        } catch (IOException e) {
            logger.info("读取excel文件失败", e);
            return "{\"msg\":\"读取excel文件失败\"}";
        }
    }



    //管理员保存当前投票结果生成excel表
    @RequestMapping(value = "excelResults", method = RequestMethod.POST)
    public String excelResults(@RequestBody String str) {
        logger.info("管理员保存当前投票结果生成excel表:" + str);
        HashMap map = JSON.parseObject(str, HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"mid","vcid"};
        if (!(JsonUtil.checkFormatStrict(str , jsonKeys))) {
            return "{\"msg\":\"请求体参数格式错误\"}";
        }

        String mid = (String) map.get("mid");
        String vcid = (String) map.get("vcid");

        //判断传入字符是否合法
        if (!(vcid.substring(0,mid.length()).equals(mid))) {
            logger.warn("传入字符不合法");
            return "{\"msg\":\"管理员id与投票id格式不合法\"}";
        }

        //判断管理员是否存在
        Manager manager = managerService.selectByPriKey(mid);
        if (manager == null) {
            logger.warn("未找到管理员" + mid);
            return "{\"msg\":\"未找到管理员\"}";
        }

        //判断投票是否存在
        Vote vote = voteService.selectByPriKey(vcid);
        if (vote == null) {
            logger.warn("未找到投票活动" + vcid);
            return "{\"msg\":\"未找到该投票活动\"}";
        }

        //通用Example进行查询
        Example example = new Example(Candidate.class);
        //selectProperties一定要放在第一个才能生效
        example.selectProperties("cId", "cInfo", "cSum", "cAbstention", "cDissenting");
        example.createCriteria()
                .andEqualTo("cMid", mid)
                .andEqualTo("cVcid", vcid);
        example.orderBy("cSum")
                .desc()
                .orderBy("cDissenting")
                .orderBy("cAbstention")
                .asc();
        List<Candidate> candidates = candidateService.selectByExample(example);
        logger.info("candidate原生对象list:" + candidates);

        String mode = vote.getVcMode();
        StringBuilder cs = new StringBuilder("[");
        for (Candidate candidate : candidates) {
            //使用LinkedHashMap解决顺序问题、使用TypeReference指明反序列化的类型
            TypeReference<LinkedHashMap<String, String>> typeReference = new TypeReference<LinkedHashMap<String, String>>() {
            };
            LinkedHashMap<String,String> hashMap = JSON.parseObject(candidate.getcInfo(), typeReference);
            String csum = String.valueOf(candidate.getcSum());
            //判断是投票还是评分
            if (mode.equals("11")) {
                hashMap.put("赞同",csum);
                hashMap.put("弃权",Integer.toString(candidate.getcAbstention()));
                hashMap.put("反对",Integer.toString(candidate.getcDissenting()));
            } else {
                hashMap.put("分数",csum);
            }
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

        JSONArray jsonArray = JSON.parseArray(cs.toString());
        logger.info("json后的字符串" + cs);

         /*
        生成excel文件的存储路径和文件名
        文件的存储路径默认为:   D:/Logging/vote1.0/files/manager/本次投票活动的管理员id/本次投票活动的id/
        文件名默认为:   本次投票活动的名称(theme).xls
         */
        //windows
        String path = "C://Logging//vote1.0//files//manager//" + vote.getVcMid() + "//" + vote.getVcId() + "//";
        //linux
        //String path = "/Logging/vote1.0/files/manager/" + vote.getVcMid() + "/" + vote.getVcId() + "/";
        String name = vote.getVcTheme();

        String head = name + "第" + vote.getVcId().split("_")[1] + "次投票";

        if (ExcelUtil.createExcel(path, name, head, jsonArray)) {
            logger.info("生成成功！");
            return "{\"msg\":\"111\",\"url\":\"" + path + name + "\"}";
        } else {
            return "{\"msg\":\"生成excel失败\"}";
        }

    }



    //文件下载
    @ResponseBody
    @RequestMapping(value = "download",method = RequestMethod.POST)
    public void download(HttpServletResponse response ,@RequestBody String str) throws Exception{
        logger.info("前端发来的路径" + str);
        HashMap map = JSON.parseObject(str , HashMap.class);
        //验证请求体中的请求个数与key名是否合法
        String [] jsonKeys = {"url"};
        JsonUtil.checkFormatStrict(str , jsonKeys);

        String url = (String) map.get("url");

        String path = url + ".xls";
        logger.info("真正的路径" + path);

        if (FileUtil.download(path,false,response)) {
            logger.info("文件下载成功");
        } else {
            logger.warn("文件下载失败");
        }
    }



    //文件在线预览(全部转换为pdf预览)
    @ResponseBody
    @RequestMapping(value = "preview", method = RequestMethod.GET)
    public void preview(HttpServletResponse response , HttpServletRequest request) throws Exception {
        String fileName = request.getParameter("filename");

        //判断文件名格式是否正确
        if (!FileUtil.checkName(fileName)) {
            logger.warn("文件名格式错误");
            return ;
        }
        //获取文件前后缀名
        String prefixName = FileUtil.getPrefixName(fileName);
        String suffixName = FileUtil.getSuffixName(fileName);

        //windows(文件的查找路径)
        String path = "C://Public//";
        //linux
        //String path = "/Public/";

        //输入输出文件
        File inFile = new File(path + fileName);
        File outFile = new File(path + prefixName + ".pdf");
        //判断源文件是否存在
        if (!inFile.exists()) {
            logger.warn("文件不存在");
            return ;
        }

        //判断文件是否需要转换
        if (!suffixName.equalsIgnoreCase("pdf") && (!outFile.exists())) {
            try {
                documentConverter.convert(inFile).to(outFile).execute();
            } catch (OfficeException e) {
                e.printStackTrace();
            }
        }

        if (FileUtil.download(outFile.getAbsolutePath(),true, response)) {
            logger.info("文件预览成功");
        } else {
            logger.warn("文件预览失败");
        }

    }






}