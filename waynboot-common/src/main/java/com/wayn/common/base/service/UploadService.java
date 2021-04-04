package com.wayn.common.base.service;

import com.qiniu.common.QiniuException;

import java.io.IOException;

/**
 * <p>
 * 七牛云上传Service
 * </p>
 *
 * @package: com.xkcoding.upload.service
 * @description: 七牛云上传Service
 * @author: yangkai.shen
 * @date: Created in 2018/11/6 17:21
 * @copyright: Copyright (c) 2018
 * @version: V1.0
 * @modified: yangkai.shen
 */
public interface UploadService {
    /**
     * 上传文件
     *
     * @param filename 文件名
     * @return 上传后的文件访问路径
     * @throws QiniuException 七牛异常
     */
    String uploadFile(String filename) throws IOException;

}
