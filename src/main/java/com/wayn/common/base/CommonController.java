package com.wayn.common.base;

import com.wayn.common.constant.SysConstants;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.util.IdUtil;
import com.wayn.common.util.R;
import com.wayn.common.util.file.FileUploadUtil;
import com.wayn.common.util.file.FileUtils;
import com.wayn.common.util.http.HttpUtil;
import com.wayn.framework.config.WaynConfig;
import com.wayn.framework.redis.RedisCache;
import com.wf.captcha.SpecCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * 通用请求处理类
 *
 * @author ruoyi
 */
@Slf4j
@Controller
@RequestMapping("common")
public class CommonController {

    @Autowired
    private RedisCache redisCache;

    /**
     * 通用下载请求
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    @GetMapping("download")
    public void fileDownload(String fileName, boolean delete, HttpServletResponse response, HttpServletRequest request) {
        try {
            if (!FileUtils.isValidFilename(fileName)) {
                throw new BusinessException("文件名称(" + fileName + ")非法，不允许下载。 ");
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = WaynConfig.getDownloadPath() + fileName;

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
            if (!FileUtils.isValidFilename(fileName)) {
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
    public R uploadFile(MultipartFile file, HttpServletRequest request) {
        try {
            // 上传文件路径
            String filePath = WaynConfig.getUploadDir();
            String fileName = FileUploadUtil.uploadFile(file, filePath);
            String requestUrl = HttpUtil.getRequestContext(request);
            String url = requestUrl + "/upload/" + fileName;
            return R.success().add("url", url).add("fileName", fileName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error(e.getMessage());
        }
    }

    @ResponseBody
    @RequestMapping("/captcha")
    public R captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SpecCaptcha specCaptcha = new SpecCaptcha(100, 43, 4);
        String verCode = specCaptcha.text().toLowerCase();
        String key = IdUtil.getUid();
        // 存入redis并设置过期时间为30分钟
        redisCache.setCacheObject(key, verCode, SysConstants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 将key和base64返回给前端
        return R.success().add("key", key).add("image", specCaptcha.toBase64());
    }
}
