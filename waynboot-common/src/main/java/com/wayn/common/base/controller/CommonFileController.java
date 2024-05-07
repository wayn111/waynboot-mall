package com.wayn.common.base.controller;

import com.alibaba.fastjson.JSONObject;
import com.wayn.common.base.service.UploadService;
import com.wayn.common.config.WaynConfig;
import com.wayn.common.core.vo.Base64FileVO;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.R;
import com.wayn.util.util.file.FileUploadUtil;
import com.wayn.util.util.file.FileUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 通用请求处理类
 */
@Slf4j
@Controller
@AllArgsConstructor
@RequestMapping("common")
public class CommonFileController {

    public static final String IMAGE_BASE64_FLAG = ";base64,";
    private UploadService uploadService;

    /**
     * 通用下载请求
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    @GetMapping("download")
    public void fileDownload(String fileName, boolean delete, HttpServletResponse response, HttpServletRequest request) {
        try {
            // 1. 判断下载文件名称是否合规
            if (!FileUtils.checkAllowDownload(fileName)) {
                throw new BusinessException("文件名称(" + fileName + ")非法，不允许下载。 ");
            }
            // 2. 获取要下载的文件所在服务器的下载目录位置
            String filePath = WaynConfig.getDownloadPath() + fileName;
            // 3. 重命令下载文件名称
            String realFileName = System.currentTimeMillis() + FilenameUtils.EXTENSION_SEPARATOR + FilenameUtils.getExtension(fileName);
            // 4. 将下载文件写入响应流并设置下载文件名称
            response.setCharacterEncoding("utf-8");
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition", "attachment;fileName=" + FileUtils.setFileDownloadHeader(request, realFileName));
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete) {
                FileUtils.deleteFile(filePath);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 下载模板文件
     *
     * @param fileName 文件名称
     */
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(String fileName, HttpServletResponse response, HttpServletRequest request) {
        try {
            if (!FileUtils.checkAllowDownload(fileName)) {
                throw new BusinessException("文件名称(" + fileName + ")非法，不允许下载。 ");
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String realPath = this.getClass().getResource("/template").getPath();
            String filePath = realPath + File.separatorChar + fileName;

            response.setCharacterEncoding("utf-8");
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition",
                    "attachment;fileName=" + FileUtils.setFileDownloadHeader(request, realFileName));
            FileUtils.writeBytes(filePath, response.getOutputStream());
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求
     */
    @PostMapping("upload")
    @ResponseBody
    public R<String> uploadFile(MultipartFile file, HttpServletRequest request) {
        try {
            // 1. 获取上传文件的保存路径
            String filePath = WaynConfig.getUploadDir();
            log.info("file path is {}", filePath);
            // 2. 上传文件至服务器
            String fileName = FileUploadUtil.uploadFile(file, filePath);
            // 3. 返回文件访问路径
            String fileUrl = "/upload/" + fileName;
            return R.success(fileUrl);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error(ReturnCodeEnum.UPLOAD_ERROR);
        }
    }

    /**
     * 通用上传请求
     */
    @PostMapping("base64uploadFile")
    @ResponseBody
    public R<String> base64uploadFile(String filename,
                                      String base64content,
                                      HttpServletRequest request) {
        try {
            byte[] decode = Base64.getDecoder().decode(base64content.substring(base64content.indexOf(IMAGE_BASE64_FLAG) + IMAGE_BASE64_FLAG.length()));
            // 上传文件路径
            String filePath = WaynConfig.getUploadDir();
            String fileName = FileUploadUtil.uploadFile(decode, filename, filePath);
            String fileUrl = uploadService.uploadFile(fileName);
            return R.success(fileUrl);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error(ReturnCodeEnum.UPLOAD_ERROR);
        }
    }

    /**
     * 通用上传请求
     */
    @PostMapping("base64uploadFileList")
    @ResponseBody
    public R<List<JSONObject>> base64uploadFileList(@RequestBody List<Base64FileVO> list, HttpServletRequest request) {
        try {
            List<JSONObject> fileList = new ArrayList<>();
            for (Base64FileVO base64FileVO : list) {
                String filename = base64FileVO.getFilename();
                String base64content = base64FileVO.getBase64content();
                byte[] decode = Base64.getDecoder().decode(base64content.substring(base64content.indexOf(IMAGE_BASE64_FLAG) + IMAGE_BASE64_FLAG.length()));
                // 上传文件路径
                String filePath = WaynConfig.getUploadDir();
                String fileName = FileUploadUtil.uploadFile(decode, filename, filePath);
                String fileUrl = uploadService.uploadFile(fileName);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("fileName", fileName);
                jsonObject.put("url", fileUrl);
                fileList.add(jsonObject);
            }
            return R.success(fileList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error(ReturnCodeEnum.UPLOAD_ERROR);
        }
    }

}
