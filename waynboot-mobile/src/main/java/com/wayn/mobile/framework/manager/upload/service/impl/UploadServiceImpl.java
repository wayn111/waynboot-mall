package com.wayn.mobile.framework.manager.upload.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.wayn.common.exception.BusinessException;
import com.wayn.common.util.ServletUtils;
import com.wayn.common.util.file.FileUtils;
import com.wayn.common.util.http.HttpUtil;
import com.wayn.mobile.framework.config.WaynConfig;
import com.wayn.mobile.framework.manager.upload.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class UploadServiceImpl implements UploadService, InitializingBean {

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private Auth auth;

    @Value("${qiniu.enabled}")
    private boolean enableQiniu;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.prefix}")
    private String prefix;

    private StringMap putPolicy;

    /**
     * 七牛云上传文件
     *
     * @param fileName 文件名
     * @return 上传后的文件访问路径
     * @throws QiniuException 七牛异常
     */
    @Override
    public String uploadFile(String fileName) {
        if (enableQiniu) {
            File file = new File(WaynConfig.getUploadDir() + File.separator + fileName);
            Response response;
            try {
                response = this.uploadManager.put(file, file.getName(), getUploadToken());
                int retry = 0;
                while (response.needRetry() && retry < 3) {
                    response = this.uploadManager.put(file, file.getName(), getUploadToken());
                    retry++;
                }
                if (response.isOK()) {
                    JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
                    String yunFileName = jsonObject.getString("key");
                    String yunFilePath = prefix + "/" + yunFileName;
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

    @Override
    public void afterPropertiesSet() {
        this.putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"width\":$(imageInfo.width), \"height\":${imageInfo.height}}");
    }

    /**
     * 获取上传凭证
     *
     * @return 上传凭证
     */
    private String getUploadToken() {
        return this.auth.uploadToken(bucket, null, 3600, putPolicy);
    }
}
