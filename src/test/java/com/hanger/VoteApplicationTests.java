package com.hanger;

import com.alibaba.fastjson.JSON;
import com.hanger.entity.Mark;
import com.hanger.entity.Voter;
import com.hanger.service.CandidateService;
import com.hanger.service.ManagerService;
import com.hanger.service.VoteService;
import com.hanger.service.VoterService;
import com.hanger.util.JsonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.regex.Pattern;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VoteApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class VoteApplicationTests {
    private Logger logger = LoggerFactory.getLogger(VoteApplicationTests.class);


    @Autowired
    ManagerService managerService;
    @Autowired
    VoteService voteService;
    @Autowired
    CandidateService candidateService;
    @Autowired
    VoterService voterService;
//    @Autowired
//    private DocumentConverter documentConverter;


    @Test
    public void fastJsonTest() {

        //查询数据库得到voter表的一条数据
        Voter voter = voterService.selectByPriKey("5555");
        logger.info("原生对象："+ voter);

        //将查出的对象直接转化为json字符串
        String s = JSON.toJSONString(voter);
        logger.info("直接转json"+ s);

        //将不想转为json的属性值置null(fastjson转json时会自动忽略)
        voter.setvPassword(null);
        voter.setvMid(null);
        voter.setvStatus(null);
        logger.info("部分置null" + voter);

        //置null后的对象再转化为json
        String s1 = JSON.toJSONString(voter);
        logger.info("部分属性置null后的对象转json"+ s1);

    }



    @Test
    public void jsonUtilTest() {

        //String str = "{\"vi\":\"22\"}";
        String str = "{\"vi\":\"22\",\"vId\":\"22\",\"vId\":\"312\",\"vId\":\"32\"}";
        logger.info("原生"+ str);

        if (JsonUtil.checkFormat(str,Voter.class)) {
            Voter voter = JSON.parseObject(str, Voter.class);
            System.out.println(voter);
        }



    }



    @Test
    public void eqTest() {

//        String fileName = "2w.d";
//        String prefix = FileUtil.getPrefixName(fileName);
//        String suffixName = FileUtil.getSuffixName(fileName);
//        System.out.println(prefix);
//        System.out.println(suffixName);
//        if (suffixName.equalsIgnoreCase("pdf")) {
//            System.out.println("hello");
//        }
//
//        String path = "a.xls";
//        File file = new File(path);
//        System.out.println(file.getName());
//        System.out.println(file.getPath());
//        System.out.println(file.getAbsolutePath());


        //输入输出文件
//        File inFile = new File("D://Public//测试" + "11.pptx");
//        File outFile = new File("D://Public//测试" + "11.pdf");
//        //判断源文件是否存在
//        if (!inFile.exists()) {
//            logger.warn("文件不存在");
//            return;
//        }
//
//        String suffixName = FileUtil.getSuffixName(inFile.getName());
//        //判断文件是否需要转换
//        if (!suffixName.equalsIgnoreCase("pdf") || (!outFile.exists())) {
//            try {
//                documentConverter.convert(inFile).to(outFile).execute();
//            } catch (OfficeException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println("ok");
    }



    @Test
    public void numTest() {

        String s = "87";
        if (Pattern.compile("[0-9]*").matcher(s).matches()) {
            int score = Integer.parseInt(s);
            System.out.println("hello" + score);
        } else {
            System.out.println("bye");
        }

    }


    @Test
    public void nullTest() {

        Mark mark = new Mark();

        System.out.println("1" + mark.getcId());
        System.out.println("2" + mark.getcScore());
        System.out.println("3" + mark);

//        if (mark == null) {
//            System.out.println("nul");
//        }
        Integer cid = mark.getcId();
        if (cid == null) {
            logger.warn("id不存在");
        }

    }




}
