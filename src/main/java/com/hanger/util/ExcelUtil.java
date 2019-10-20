package com.hanger.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

public class ExcelUtil {
    private static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);



    /**
     *
     * @param file excel文件
     * @param fileName 文件名
     * @param type 文件类型
     * @return Boolean 是否为可用的excel文件
     */
    private static Boolean checkFile(MultipartFile file, String fileName, String type) {
        //判断文件是否存在
        if(file.isEmpty()){
            logger.error("文件不存在！！！");
            return false;
        }

        //判断文件是否是excel文件
        if ((type.equals("xls"))||type.equals("xlsx")){
            logger.info("文件名：" + fileName);
            logger.info("类型：" + type + "文件");
            logger.info("大小：" + file.getSize() + "字节");
            return true;
        }else {
            logger.error("不是excel文件！！！");
            logger.info("文件名：" + fileName);
            logger.info("类型：" + type + "文件");
            return false;
        }

    }



    /**
     *
     * @param file excel文件
     * @param type 文件类型
     * @return Workbook 返回一个excel对象
     */
    private static Workbook getWorkBook(MultipartFile file, String type) {

        //创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        try {
            //获取excel文件的io流
            InputStream is = file.getInputStream();
            //根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
            if(type.equals("xls")){
                workbook = new HSSFWorkbook(is);
            }else if(type.equals("xlsx")){
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return workbook;
    }



    /**
     * 创建一个单元格并为其设定指定的对齐方式
     * @param wb excel文件对象
     * @param row 行对象
     * @param column 行对象的列索引（从零开始）
     * @param value 单元格的内容（字符串类型）
     * @param halign 水平方向对其方式
     * @param valign 垂直方向对其方式
     */
    private static void createCell(Workbook wb,Row row,short column, String value ,HorizontalAlignment halign,VerticalAlignment valign) {
        //创建单元格
        Cell cell = row.createCell(column);
        //设置值
        cell.setCellValue(new HSSFRichTextString(value));
        //创建单元格样式
        CellStyle cellStyle = wb.createCellStyle();
        //设置单元格水平方向对其方式
        cellStyle.setAlignment(halign);
        //设置单元格垂直方向对其方式
        cellStyle.setVerticalAlignment(valign);
        //设置单元格样式
        cell.setCellStyle(cellStyle);
    }



    /**
     *
     * @param filepath 文件路径(注意结尾带上"/") 比如：D:/tomcat/webapps/Logging/
     * @param filename 文件名 比如：test
     * @param wb excel的文档对象
     * @return 是否正常生成
     */
    private static boolean saveFile(String filepath, String filename, HSSFWorkbook wb) {
        //创建文件生成目录
        File file = new File(filepath);
        if (!file.exists()) {
            if (file.mkdirs()) {
                logger.info("文件路径生成成功！");
            } else {
                logger.info("文件路径生成失败！");
                return false;
            }
        }

        //创建文件并写入磁盘
        FileOutputStream output;
        try {
            output = new FileOutputStream(filepath + filename + ".xls");
            wb.write(output);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }



    /**
     *
     * @param file excel文件
     * @return List<String[]> 每个String数组存放的是excel表除表头外的每行的值
     * @throws IOException 抛出IO异常
     */
    public static List<String[]> readExcel(MultipartFile file) throws IOException {

        //创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回
        List<String[]> list = new ArrayList<>();

        //获取文件信息
        String fileName = file.getOriginalFilename();
        //判断文件是否有名字
        if (fileName == null) {
            logger.warn("文件名不存在！");
            return list;
        }
        String type = fileName.substring(fileName.lastIndexOf(".") + 1);

        //检查文件是否可用
        if (!(checkFile(file,fileName,type))) {
            list.add(new String[]{"失败！"});
            return list;
        }
        //获得Workbook工作薄对象
        Workbook workbook = getWorkBook(file,type);

        if(workbook != null) {
            for(int sheetNum = 0;sheetNum < workbook.getNumberOfSheets();sheetNum++) {
                //获得当前sheet工作表
                Sheet sheet = workbook.getSheetAt(sheetNum);

                if(sheet != null) {
                    //获得当前sheet的开始行
                    int firstRowNum  = sheet.getFirstRowNum();
                    //获得当前sheet的结束行
                    int lastRowNum = sheet.getLastRowNum();
                    //循环除第一行
                    for(int rowNum = firstRowNum+1;rowNum <= lastRowNum;rowNum++) {
                        //获得当前行
                        Row row = sheet.getRow(rowNum);
                        if(row != null) {
                            //获得当前行的开始列
                            int firstCell = row.getFirstCellNum();
                            //获得当前行的列数
                            int CellNum = row.getPhysicalNumberOfCells();
                            String[] cells = new String[CellNum];
                            //循环当前行
                            for(int c = 0; c < CellNum;c++) {
                                Cell cell = row.getCell(firstCell);
                                cell.setCellType(CellType.STRING);
                                cells[c] = cell.getStringCellValue();
                                ++firstCell;
                            }
                            list.add(cells);
                        }
                    }

                }

            }
            workbook.close();
        }

        return list;
    }



    /**
     *方法重载 带title的
     *@param filepath 文件路径(注意结尾带上"/") 比如：D:/tomcat/webapps/Logging/
     *@param filename 文件名 比如：test
     *@param head 第一行的标题栏,与内容无关（可以写一些声明或者其他必要字段）
     *@param values JSONArray值集
     *@return 是否正常生成
     */
    public static boolean createExcel(String filepath, String filename, String head, JSONArray values) {
        //创建excel的文档对象
        HSSFWorkbook wb = new HSSFWorkbook();
        //创建excel的文档对象的sheet对象
        HSSFSheet sheet = wb.createSheet("sheet0");

        //创建sheet对象的第一行的标题栏并设置行高
        Row title = sheet.createRow(0);
        title.setHeightInPoints(30);
        // 创建第一列
        createCell(wb, title, (short)0,head,HorizontalAlignment.CENTER,VerticalAlignment.CENTER);
        //获取数据行的宽度
        //使用LinkedHashMap解决顺序问题、使用TypeReference指明反序列化的类型
        TypeReference<LinkedHashMap<String, Object>> typeReference = new TypeReference<LinkedHashMap<String, Object>>() {
        };
        LinkedHashMap<String,Object> map = JSON.parseObject(JSON.toJSONString(values.get(0)), typeReference);
        // 合并单元格.起始行号，终止行号，起始列号，终止列号（这里参照数据行的宽度合并）
        CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, map.size() - 1);
        sheet.addMergedRegion(cellRangeAddress);

        //创建sheet对象的第二行的数据(属性行)
        HSSFRow field = sheet.createRow(1);
        Set<String> set = map.keySet();
        int i = 0;
        String [] keys = new String[set.size()];
        for (String key : set) {
            //System.out.println("key:" + key);
            keys[i] = key;
            field.createCell(i).setCellValue(key);
            ++i;
        }

        //创建sheet对象的值行
        for(int row = 0;row < values.size();row++) {
            //值行从属性行的下一行开始（这里是第3行）
            HSSFRow rows = sheet.createRow(row + 2);
            //获取该行的数据
            LinkedHashMap value = JSON.parseObject(JSON.toJSONString(values.get(row)), LinkedHashMap.class);
            //创建sheet对象的值列
            //logger.info("创建sheet对象的值列");
            for(int col = 0;col < value.size();col++){
                Object o = value.get(keys[col]);
                rows.createCell(col).setCellValue(o.toString());
            }
        }

        return saveFile(filepath, filename, wb);

    }



    /**
     *方法重载 不带title的
     *@param filepath 文件路径(注意结尾带上"/") 比如：D:/tomcat/webapps/Logging/
     *@param filename 文件名 比如：test
     *@param values JSONArray值集
     *@return 是否正常生成
     */
    public static boolean createExcel(String filepath, String filename, JSONArray values) {
        //创建excel的文档对象
        HSSFWorkbook wb = new HSSFWorkbook();
        //创建excel的文档对象的sheet对象
        HSSFSheet sheet = wb.createSheet("sheet0");

        //创建sheet对象的第一行的数据(属性行)
        HSSFRow field = sheet.createRow(0);
        //使用LinkedHashMap解决顺序问题、使用TypeReference指明反序列化的类型
        TypeReference<LinkedHashMap<String, Object>> typeReference = new TypeReference<LinkedHashMap<String, Object>>() {
        };
        LinkedHashMap<String,Object> map = JSON.parseObject(JSON.toJSONString(values.get(0)), typeReference);
        Set<String> set = map.keySet();
        int i = 0;
        String [] keys = new String[set.size()];
        for (String key : set) {
            //System.out.println("key:" + key);
            keys[i] = key;
            field.createCell(i).setCellValue(key);
            ++i;
        }

        //创建sheet对象的值行
        for(int row = 0;row < values.size();row++) {
            //值行从属性行的下一行开始（这里是第2行）
            HSSFRow rows = sheet.createRow(row + 1);
            //获取该行的数据
            LinkedHashMap value = JSON.parseObject(JSON.toJSONString(values.get(row)), LinkedHashMap.class);
            //创建sheet对象的值列
            //logger.info("创建sheet对象的值列");
            for(int col = 0;col < value.size();col++){
                Object o = value.get(keys[col]);
                rows.createCell(col).setCellValue(o.toString());
            }
        }

        return saveFile(filepath, filename, wb);

    }



    /**
     *方法重载 只有一个属性行,一般用做示例文件
     *@param filepath 文件路径(注意结尾带上"/") 比如：D:/tomcat/webapps/Logging/
     *@param filename 文件名 比如：test
     *@param fields JSONArray值集
     *@return 是否正常生成
     */
    public static boolean createExcel(String filepath, String filename, String[] fields) {
        //创建excel的文档对象
        HSSFWorkbook wb = new HSSFWorkbook();
        //创建excel的文档对象的sheet对象
        HSSFSheet sheet = wb.createSheet("sheet0");

        //创建sheet对象的第一行的数据(属性行)
        HSSFRow field = sheet.createRow(0);

        for (int f = 0;f < fields.length;f++) {
            field.createCell(f).setCellValue(fields[f]);
        }

        return saveFile(filepath, filename, wb);
    }







}
