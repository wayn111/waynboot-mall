package com.wayn.common.util.excel;

import com.alibaba.excel.EasyExcel;
import com.wayn.common.util.ServletUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * excel操作帮助类
 */
@Slf4j
public class ExcelUtil {

    /**
     * 编码文件名
     *
     * @param filename 源文件名称
     * @return 编码后文件名 eg: 990xx002_测试.xlsx
     */
    public static String encodingFilename(String filename) {
        filename = UUID.randomUUID() + "_" + filename;
        return filename;
    }

    /**
     * 获取文件绝对路径
     *
     * @param filename 文件
     * @return 绝对路径
     */
    public static String getAbsoluteFile(String filename, String path) {
        String downloadPath = path + File.separator + filename;
        File desc = new File(downloadPath);
        if (!desc.getParentFile().exists()) {
            desc.getParentFile().mkdirs();
        }
        return downloadPath;
    }

    /**
     * 导出excel到指定文件中
     *
     * @param list         excel数据
     * @param tClass       解析对象类型
     * @param fileName 文件名
     * @return 返回指定文件的名称
     */
    public static <T> String exportExcel(List<T> list, Class<T> tClass, String fileName, String path) {
        String filename = ExcelUtil.encodingFilename(fileName);
        EasyExcel.write(ExcelUtil.getAbsoluteFile(filename, path), tClass).sheet("sheet").doWrite(list);
        return filename;
    }

    /**
     * 导出excel到输出流
     *
     * @param response web输出流
     * @param list     excel数据
     * @param tClass   解析对象类型
     */
    public static <T> void exportExcel(HttpServletResponse response, List<T> list, Class<T> tClass, String fileName) {

        try (ServletOutputStream outputStream = response.getOutputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            // 使用bos获取excl文件大小
            EasyExcel.write(bos, tClass).autoCloseStream(Boolean.FALSE).sheet("sheet").doWrite(list);
            ServletUtils.setExportResponse(response, fileName, bos.size());
            bos.writeTo(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
