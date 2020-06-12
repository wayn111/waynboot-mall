package com.wayn.common.util.excel;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
        filename = UUID.randomUUID().toString() + "_" + filename;
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
     * @param originalName 文件名
     * @return 返回指定文件的名称
     */
    public static <T> String exportExcel(List<T> list, Class<T> tClass, String originalName, String path) {
        ExportParams exportParams = new ExportParams();
        exportParams.setStyle(IExcelExportStylerImpl.class);
        Workbook workbook = ExcelExportUtil.exportExcel(exportParams,
                tClass, list);
        String filename = ExcelUtil.encodingFilename(originalName);
        try (OutputStream out = new FileOutputStream(ExcelUtil.getAbsoluteFile(filename, path))) {
            workbook.write(out);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return filename;
    }
}
