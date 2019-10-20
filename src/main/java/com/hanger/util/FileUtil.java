package com.hanger.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);



    /**
     * 扫描指定文件夹从而获取该文件夹下的所有文件的文件名
     * 注： 不包含子文件夹下的文件名
     *     不包含文件夹名
     * @param path 要扫描的文件夹的绝对路径
     * @return 该文件夹下的所有文件的文件名组成的数组 | null
     */
    public static ArrayList<String> getFilenames(String path) {
        ArrayList<String> filenames = new ArrayList<>();

        File file = new File(path);
        File[] files = file.listFiles();
        //判断文件夹是否为空
        if (files == null) {
            logger.warn("扫描的文件夹为空");
            return null;
        }

        for (File f : files) {
            if (f.isFile()) {
                filenames.add(f.getName());
            }
        }
        return filenames;
    }



//    /**
//     * 扫描指定文件夹从而获取该文件夹下的所有文件的文件名
//     * 注： 包含子文件夹下的文件名
//     *     不包含文件夹名
//     * @param path 要扫描的文件夹的绝对路径
//     * @param fileName 存放该文件夹下的所有文件的文件名的数组
//     */
//    public static void getAllFilenames(String path, ArrayList<String> fileName) {
//        File file = new File(path);
//        File [] files = file.listFiles();
//
//        if (files != null) {
//            for(File a:files) {
//                if(a.isDirectory()) {
//                    getAllFileName(a.getAbsolutePath(),fileName);
//                }
//            }
//        }
//
//        String [] names = file.list();
//        if(names != null)
//            fileName.addAll(Arrays.asList(names));
//    }



    /**
     * 检查文件名是否合法
     * 要求文件名必须是: XXX.XXX的一般格式
     * @param fileName 要检查的文件名
     * @return 符合返回true
     */
    public static Boolean checkName(String fileName) {
        int lp = fileName.lastIndexOf(".");
        if ((lp < 1) || (lp >= (fileName.length() - 1))) {
            logger.warn("文件名格式错误");
            return false;
        }
        return true;
    }



    /**
     * 获取文件的前缀名
     * @param fileName 要获取的文件名
     * @return 文件的前缀名
     */
    public static String getPrefixName(String fileName) {
        int lp = fileName.lastIndexOf(".");
        return fileName.substring(0,lp);
    }



    /**
     * 获取文件的后缀名
     * @param fileName 要获取的文件名
     * @return 文件的后缀名
     */
    public static String getSuffixName(String fileName) {
        int lp = fileName.lastIndexOf(".");
        return fileName.substring(lp + 1);
    }



    /**
     *
     * @param filePath 这里的路径是带文件名的绝对路径
     * @param onLine 是否是在线预览（true为在线预览 false为下载方式）
     * @param response 前端的请求体
     * @throws Exception IO异常
     */
    public static Boolean download(String filePath , boolean onLine , HttpServletResponse response) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.warn("服务器上找不到对应文件");
            response.sendError(404, "服务器上找不到对应文件");
            return false;
        }
        String fileName = file.getName();
        fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] buf = new byte[1024];
        int len;

        response.reset();
        if (onLine) {
            //在线预览方式,文件名编码必须是UTF-8
            URL u = new URL("file:///" + filePath);
            response.setContentType(u.openConnection().getContentType());
            response.setHeader("Content-Disposition", "inline; filename=" + fileName);
        } else {
            //下载方式
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        }

        //指定允许其他域名访问
        response.setHeader("Access-Control-Allow-Origin", "*");//此句是关键,其他不重要
        //响应类型
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, DELETE");
        //响应头设置
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, x-requested-with, X-Custom-Header, HaiYi-Access-Token");

        OutputStream out = response.getOutputStream();
        while ((len = bis.read(buf)) > 0)
            out.write(buf, 0, len);
        bis.close();
        out.close();

        return true;
    }





}


