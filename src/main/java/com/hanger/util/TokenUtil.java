package com.hanger.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

/**
 * @author hanger
 * 2019-08-27 11:37
 */
public class TokenUtil {
    private static Logger logger = LoggerFactory.getLogger(TokenUtil.class);


    /**
     *
     * @return 随机生成6位字符串(0-1,a-Z)
     */
    private static String createRandom() {
        StringBuilder code = new StringBuilder();
        //随机字符池
        String model = "0123456789aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ";
        char[] m = model.toCharArray();
        for (int i = 0; i < 6; i++) {
            code.append(m[(int) (Math.random() * 62)]);
        }
        logger.info("生成六位随机token:" + code);
        return code.toString();
    }



    /**
     * 生成随机session值
     * @param id 用户id
     * @return 加密的id-时间-六位随机字母与数字
     */
    public static String createSession(String id) {
        StringBuilder session = new StringBuilder();
        //加密的用户名
        session.append(DigestUtils.md5Hex(id)).append("-");
        //时间
        session.append(new Date()).append("-");
        //随机生成6位字符串(0-1,a-Z)
        session.append(createRandom());
        logger.info("生成session:" + session.toString());
        return session.toString();
    }






}


