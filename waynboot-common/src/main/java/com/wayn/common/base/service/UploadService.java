package com.wayn.common.base.service;

import java.io.IOException;

public interface UploadService {
    /**
     * 上传文件
     *
     * @param filename 文件名
     * @return 上传后的文件访问路径
     */
    String uploadFile(String filename) throws IOException;

}
