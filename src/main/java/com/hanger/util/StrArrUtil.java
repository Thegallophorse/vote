package com.hanger.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrArrUtil {
    private static Logger logger = LoggerFactory.getLogger(StrArrUtil.class);


    public static String [] stringToArray(String str) {
        String [] arr;

        //System.out.println("in:" + str);
        char begin = '[';
        char end = ']';
        //判断字符串的大小
        int size = str.length();
        //判断起止位置是否为[]
        if ((str.charAt(0) != begin) || (str.charAt(size-1) != end) || (size == 2)){
            logger.warn("not str");
            return null;
        }
        //获取中间的内容
        String cont = str.substring(1,size-1);

        //用,分割并返回字符数组
        arr = cont.split(",");
        for (int i = 0; i < arr.length; i++) {
            int ssize = arr[i].length();
            String scont = arr[i].substring(1,ssize-1);
            arr[i] = scont;
            //System.out.println(arr[i]);
        }

        return arr;
    }






}
