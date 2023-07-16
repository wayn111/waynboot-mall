package com.wayn.common.base.service;

import com.qiniu.common.QiniuException;

import java.io.IOException;

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
