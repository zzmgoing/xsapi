package com.zzming.xsapi.util.excel;

import com.zzming.xsapi.util.FileUtil;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Excel2CsvUtil {

    public static String excel2csv(File file) {
        if (file == null) {
            return null;
        }
        try {
            return parseExcel(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String parseExcel(File file) throws Exception {
        Workbook xSSFWorkbook;
        FileInputStream inputStream = new FileInputStream(file);
        boolean isExcel2003 = file.getName().toLowerCase().endsWith("xls");
        if (isExcel2003) {
            xSSFWorkbook = new HSSFWorkbook(inputStream);
        } else {
            xSSFWorkbook = new XSSFWorkbook(inputStream);
        }
        Iterator<Sheet> sheetIterator = xSSFWorkbook.sheetIterator();
        ArrayList<SheetBean> sheetBeans = new ArrayList<SheetBean>();
        while (sheetIterator.hasNext()) {
            Sheet sheet = (Sheet) sheetIterator.next();
            sheetBeans.add(parseSheet(sheet));
        }
        inputStream.close();
        xSSFWorkbook.close();
        return createExcel(sheetBeans, file.getName());
    }

    private static SheetBean parseSheet(Sheet sheet) {
        SheetBean sheetBean = new SheetBean();
        LinkedHashMap<String, String> products = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> modules = new LinkedHashMap<String, String>();
        LinkedHashSet<String> functionSet = new LinkedHashSet<String>();
        LinkedHashMap<String, String> aboutMap = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> stepMap = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> resultMap = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> type = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> useType = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> priority = new LinkedHashMap<String, String>();
        for (int m = 1; m < sheet.getLastRowNum() + 1; m++) {
            Row row = sheet.getRow(m);
            if(row == null){
                continue;
            }
            StringBuilder key = new StringBuilder();
            StringBuilder aboutBuilder = new StringBuilder();
            String product = "";
            String module = "";
            for (Cell cell : row) {
                String value;
                int n = cell.getColumnIndex();
                boolean isMerge = isMergedRegion(sheet, m, n);
                if (isMerge) {
                    value = getMergedRegionValue(sheet, row.getRowNum(), n);
                } else {
                    System.out.println("检测：" + getCellValue(cell));
                    value = getCellValue(cell) + "";
                }
                if (n == 0) {
                    product = value;
                    continue;
                }
                if (n == 1) {
                    module = value;
                    continue;
                }
                if (n > 1 && n < 8) {
                    if (n != 7) {
                        value = value + "_";
                    }
                    key.append(value);
                    continue;
                }
                if (n > 7 && n < 13) {
                    value = value + "_";
                    aboutBuilder.append(value);
                    if (n == 12) {
                        functionSet.add(key.toString());
                        products.put(key.toString(), product);
                        modules.put(key.toString(), module);
                        aboutMap.computeIfAbsent(key.toString(), k -> aboutBuilder.toString());
                    }
                    continue;
                }
                if (n == 13) {
                    stepMap.merge(key.toString(), value, (a, b) -> a + "\n" + b);
                    continue;
                }
                if (n == 14) {
                    resultMap.merge(key.toString(), value, (a, b) -> a + "\n" + b);
                    continue;
                }
                if (n == 15) {
                    priority.put(key.toString(), value);
                    continue;
                }
                if (n == 16) {
                    type.put(key.toString(), value);
                    continue;
                }
                if (n == 17) {
                    useType.put(key.toString(), value);
                }
            }
        }
        sheetBean.setSheetName(sheet.getSheetName());
        sheetBean.setProducts(products);
        sheetBean.setModules(modules);
        sheetBean.setFunctionSet(functionSet);
        sheetBean.setAboutMap(aboutMap);
        sheetBean.setStepMap(stepMap);
        sheetBean.setResultMap(resultMap);
        sheetBean.setPriority(priority);
        sheetBean.setType(type);
        sheetBean.setUseType(useType);
        return sheetBean;
    }

    private static String createExcel(List<SheetBean> sheetBeans, String fileName) throws IOException {
        Workbook xSSFWorkbook;
        boolean isExcel2003 = fileName.toLowerCase().endsWith("xls");
        if (isExcel2003) {
            xSSFWorkbook = new HSSFWorkbook();
        } else {
            xSSFWorkbook = new XSSFWorkbook();
        }
        CreationHelper createHelper = xSSFWorkbook.getCreationHelper();
        Font font = xSSFWorkbook.createFont();
        font.setFontHeightInPoints((short) 10);
//        font.setColor('翿');
        font.setFontName("黑体");
        font.setBold(true);
        font.setItalic(false);
        CellStyle cellStyle = xSSFWorkbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setWrapText(true);
        cellStyle.setFont(font);
        for (SheetBean sheetBean : sheetBeans) {
            Sheet sheet = xSSFWorkbook.createSheet(sheetBean.getSheetName());
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(2.0F * sheet.getDefaultRowHeightInPoints());
            for (int n = 0; n < 20; n++) {
                Cell cell = titleRow.createCell(n);
                switch (n) {
                    case 0:
                        cell.setCellValue("用例编号");
                        break;
                    case 1:
                        cell.setCellValue("所属产品");
                        break;
                    case 2:
                        cell.setCellValue("所属模块");
                        break;
                    case 3:
                        cell.setCellValue("相关需求");
                        break;
                    case 4:
                        cell.setCellValue("用例标题");
                        break;
                    case 5:
                        cell.setCellValue("前置条件");
                        break;
                    case 6:
                        cell.setCellValue("步骤");
                        break;
                    case 7:
                        cell.setCellValue("预期");
                        break;
                    case 8:
                        cell.setCellValue("关键词");
                        break;
                    case 9:
                        cell.setCellValue("优先级");
                        break;
                    case 10:
                        cell.setCellValue("用例类型");
                        break;
                    case 11:
                        cell.setCellValue("适用阶段");
                        break;
                    case 12:
                        cell.setCellValue("用例状态");
                        break;
                    case 13:
                        cell.setCellValue("结果");
                        break;
                    case 14:
                        cell.setCellValue("由谁创建");
                        break;
                    case 15:
                        cell.setCellValue("创建日期");
                        break;
                    case 16:
                        cell.setCellValue("最后修改者");
                        break;
                    case 17:
                        cell.setCellValue("修改日期");
                        break;
                    case 18:
                        cell.setCellValue("用例版本");
                        break;
                    case 19:
                        cell.setCellValue("相关用例");
                        break;
                }
                cell.setCellStyle(cellStyle);
                sheet.autoSizeColumn(n);
            }
            LinkedHashMap<String, String> products = sheetBean.getProducts();
            LinkedHashMap<String, String> modules = sheetBean.getModules();
            LinkedHashSet<String> functionSet = sheetBean.getFunctionSet();
            LinkedHashMap<String, String> aboutMap = sheetBean.getAboutMap();
            LinkedHashMap<String, String> stepMap = sheetBean.getStepMap();
            LinkedHashMap<String, String> resultMap = sheetBean.getResultMap();
            LinkedHashMap<String, String> priority = sheetBean.getPriority();
            LinkedHashMap<String, String> type = sheetBean.getType();
            LinkedHashMap<String, String> useType = sheetBean.getUseType();
            Iterator<String> keyIterator = functionSet.iterator();
            int i = 1;
            while (keyIterator.hasNext()) {
                String key = (String) keyIterator.next();
                Row row = sheet.createRow(i);
                row.setHeightInPoints(2.0F * sheet.getDefaultRowHeightInPoints());
                font.setBold(false);
                cellStyle.setFont(font);
                for (int n = 0; n < 20; n++) {
                    Cell cell = row.createCell(n);
                    switch (n) {
                        case 0:
                            cell.setCellValue("");
                            break;
                        case 1:
                            cell.setCellValue((String) products.get(key) + "");
                            break;
                        case 2:
                            cell.setCellValue((String) modules.get(key) + "");
                            break;
                        case 3:
                            cell.setCellValue("0");
                            break;
                        case 4:
                            cell.setCellValue(key);
                            break;
                        case 5:
                            if (isExcel2003) {
                                cell.setCellValue(new HSSFRichTextString(changeAboutStr((String) aboutMap.get(key))));
                                break;
                            }
                            cell.setCellValue(new XSSFRichTextString(changeAboutStr((String) aboutMap.get(key))));
                            break;

                        case 6:
                            if (isExcel2003) {
                                cell.setCellValue(new HSSFRichTextString((String) stepMap.get(key)));
                                break;
                            }
                            cell.setCellValue(new XSSFRichTextString((String) stepMap.get(key)));
                            break;

                        case 7:
                            if (isExcel2003) {
                                cell.setCellValue(new HSSFRichTextString((String) resultMap.get(key)));
                                break;
                            }
                            cell.setCellValue(new XSSFRichTextString((String) resultMap.get(key)));
                            break;

                        case 8:
                            cell.setCellValue("");
                            break;
                        case 9:
                            cell.setCellValue((String) priority.get(key) + "");
                            break;
                        case 10:
                            cell.setCellValue((String) type.get(key) + "");
                            break;
                        case 11:
                            cell.setCellValue((String) useType.get(key) + "");
                            break;
                        case 12:
                            cell.setCellValue("");
                            break;
                        case 13:
                            cell.setCellValue("");
                            break;
                        case 14:
                            cell.setCellValue("");
                            break;
                        case 15:
                            cell.setCellValue("");
                            break;
                        case 16:
                            cell.setCellValue("");
                            break;
                        case 17:
                            cell.setCellValue("");
                            break;
                        case 18:
                            cell.setCellValue("");
                            break;
                        case 19:
                            cell.setCellValue("");
                            break;
                    }
                    cell.setCellStyle(cellStyle);
                    sheet.autoSizeColumn(n);
                }
                int enterCnt = 0;
                for (int j = 5; j <= 7; j++) {
                    int rwsTemp = row.getCell(j).toString().split("\n").length;
                    if (rwsTemp > enterCnt) {
                        enterCnt = rwsTemp;
                    }
                }
                row.setHeight((short) (enterCnt * 250));
                i++;
            }
        }
        String newFileName = FileUtil.getPublicPath() + "【禅道】" + fileName;
        FileOutputStream fileOutputStream = new FileOutputStream(newFileName);
        xSSFWorkbook.write(fileOutputStream);
        fileOutputStream.close();
        xSSFWorkbook.close();
        String csvName1 = newFileName.replace("【禅道】", "【禅道CSV】");
        String csvName2 = csvName1.substring(0, csvName1.indexOf(".")) + ".csv";
        try {
            if (isExcel2003) {
                XLS2CSV xls2csv = new XLS2CSV(newFileName, csvName2);
                xls2csv.process();
            } else {
                XLSX2CSV xlsx2csv = new XLSX2CSV(newFileName, csvName2);
                xlsx2csv.process();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        FileUtil.deleteFile(new File(newFileName));
        String downloadName = "【禅道CSV】" + fileName.substring(0,fileName.indexOf(".")) + ".csv";
        return "http://10.200.43.253:8088/download/file/" + downloadName;
    }

    private static String getCellValue(Cell cell) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        String cellValue = "";
        CellType cellType = cell.getCellType();
        if (cellType == CellType.STRING) {
            cellValue = cell.getStringCellValue();
        } else if (cellType == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                cellValue = fmt.format(cell.getDateCellValue());
            } else {
                cellValue = String.valueOf((int) cell.getNumericCellValue());
            }
        } else if (cellType == CellType.BOOLEAN) {
            cellValue = String.valueOf(cell.getBooleanCellValue());
        } else if (cellType == CellType.BLANK) {
            cellValue = cell.getStringCellValue();
        } else if (cellType == CellType.ERROR) {
            cellValue = "错误";
        } else if (cellType == CellType.FORMULA) {
            cellValue = "错误";
        } else {
            cellValue = "错误";
        }
        return cellValue;
    }

    private static String changeAboutStr(String str) {
        StringBuffer sb = new StringBuffer();
        String[] split = str.split("_");
        for (int i = 0; i < split.length; i++) {
            String is = (split[i].equals("P") || split[i].equals("是")) ? "是" : "否";
            if (i == 0) {
                sb.append("1、涉及短信、站内信：").append(is).append("\n");
            } else if (i == 1) {
                sb.append("2、涉及运营机制：").append(is).append("\n");
            } else if (i == 2) {
                sb.append("3、涉及合同、协议：").append(is).append("\n");
            } else if (i == 3) {
                sb.append("4、涉及积分经验值变更：").append(is).append("\n");
            } else if (i == 4) {
                sb.append("5、涉及佣金：").append(is);
            }
        }
        return sb.toString();
    }

    private static boolean isMergedRegion(Sheet sheet, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if (row >= firstRow && row <= lastRow &&
                    column >= firstColumn && column <= lastColumn) {
                return true;
            }
        }
        return false;
    }

    private static String getMergedRegionValue(Sheet sheet, int row, int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress ca = sheet.getMergedRegion(i);
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();
            if (row >= firstRow && row <= lastRow &&
                    column >= firstColumn && column <= lastColumn) {
                Row fRow = sheet.getRow(firstRow);
                Cell fCell = fRow.getCell(firstColumn);
                return getCellValue(fCell);
            }
        }
        return null;
    }
}
