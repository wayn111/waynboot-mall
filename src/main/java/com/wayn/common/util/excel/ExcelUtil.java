package com.wayn.common.util.excel;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.wayn.framework.config.WaynConfig;
import com.wayn.project.system.domain.SysRole;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
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
    public static String getAbsoluteFile(String filename) {
        String downloadPath = WaynConfig.getDownloadPath() + filename;
        File desc = new File(downloadPath);
        if (!desc.getParentFile().exists()) {
            desc.getParentFile().mkdirs();
        }
        return downloadPath;
    }

    /**
     * 导出excel到指定文件中
     * @param list excel数据
     * @param originalName 文件名
     * @return 返回指定文件的名称
     */
    public static String exportExcel(List<SysRole> list, String originalName) {
        ExportParams exportParams = new ExportParams();
        exportParams.setStyle(IExcelExportStylerImpl.class);
        Workbook workbook = ExcelExportUtil.exportExcel(exportParams,
                SysRole.class, list);
        String filename = ExcelUtil.encodingFilename(originalName);
        try (OutputStream out = new FileOutputStream(ExcelUtil.getAbsoluteFile(filename))) {
            workbook.write(out);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (IOException exception) {
            log.error(exception.getMessage(), exception);
        }
        return filename;
    }
}
