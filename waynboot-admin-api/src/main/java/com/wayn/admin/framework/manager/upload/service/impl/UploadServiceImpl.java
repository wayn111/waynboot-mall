package com.wayn.admin.framework.manager.upload.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.wayn.admin.framework.config.WaynConfig;
import com.wayn.admin.framework.manager.upload.service.UploadService;
import com.wayn.common.core.domain.tool.QiniuConfig;
import com.wayn.common.core.service.tool.IQiniuConfigService;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.util.ServletUtils;
import com.wayn.common.util.file.FileUtils;
import com.wayn.common.util.file.QiniuUtil;
import com.wayn.common.util.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * <p>
 * 七牛云上传Service
 * </p>
 *
 * @package: com.xkcoding.upload.service.impl
 * @description: 七牛云上传Service
 * @author: yangkai.shen
 * @date: Created in 2018/11/6 17:22
 * @copyright: Copyright (c) 2018
 * @version: V1.0
 * @modified: yangkai.shen
 */
@Service
@Slf4j
public class UploadServiceImpl implements UploadService {

    @Autowired
    private IQiniuConfigService iQiniuConfigService;

    /**
     * 七牛云上传文件
     *
     * @param fileName 文件名
     * @return 上传后的文件访问路径
     * @throws QiniuException 七牛异常
     */
    @Override
    public String uploadFile(String fileName) {
        QiniuConfig qiniuConfig = iQiniuConfigService.getById(1);
        if (qiniuConfig != null && qiniuConfig.getEnable()) {
            File file = new File(WaynConfig.getUploadDir() + File.separator + fileName);
            Configuration cfg = new Configuration(QiniuUtil.getRegion(qiniuConfig.getRegion()));
            UploadManager uploadManager = new UploadManager(cfg);
            Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
            String upToken = auth.uploadToken(qiniuConfig.getBucket());
            Response response;
            try {
                response = uploadManager.put(file, file.getName(), upToken);
                int retry = 0;
                while (response.needRetry() && retry < 3) {
                    response = uploadManager.put(file, file.getName(), upToken);
                    retry++;
                }
                if (response.isOK()) {
                    JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
                    String yunFileName = jsonObject.getString("key");
                    String yunFilePath = qiniuConfig.getHost() + "/" + yunFileName;
                    FileUtils.deleteQuietly(file);
                    log.info("【文件上传至七牛云】绝对路径：{}", yunFilePath);
                    return yunFilePath;
                } else {
                    log.error("【文件上传至七牛云】失败，{}", JSONObject.toJSONString(response));
                    FileUtils.deleteQuietly(file);
                    throw new BusinessException("文件上传至七牛云失败");
                }
            } catch (QiniuException e) {
                FileUtils.deleteQuietly(file);
                throw new BusinessException("文件上传至七牛云失败");
            }
        } else {
            String requestUrl = HttpUtil.getRequestContext(ServletUtils.getRequest());
            return requestUrl + "/upload/" + fileName;
        }
    }

}
