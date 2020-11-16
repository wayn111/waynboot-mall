package com.wayn.admin.api.controller.tool;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.wayn.admin.framework.manager.upload.service.UploadService;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.tool.QiniuConfig;
import com.wayn.common.core.domain.tool.QiniuContent;
import com.wayn.common.core.service.tool.IQiniuConfigService;
import com.wayn.common.core.service.tool.IQiniuContentService;
import com.wayn.common.util.R;
import com.wayn.common.util.file.FileUtils;
import com.wayn.common.util.file.QiniuUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping("tool/qiniu")
public class QiniuController extends BaseController {

    @Autowired
    private IQiniuConfigService iQiniuConfigService;

    @Autowired
    private IQiniuContentService iQiniuContentService;

    @Autowired
    private UploadService uploadService;

    @GetMapping("/list")
    public R list(QiniuContent qiniuContent) {
        Page<QiniuContent> page = getPage();
        return R.success().add("page", iQiniuContentService.listPage(page, qiniuContent));
    }

    @GetMapping("config")
    public R info() {
        return R.success().add("data", iQiniuConfigService.getById(1));
    }

    @PutMapping("config")
    public R update(@Valid @RequestBody QiniuConfig qiniuConfig) {
        qiniuConfig.setId(1L);
        iQiniuConfigService.updateById(qiniuConfig);
        return R.success("修改成功");
    }

    @PostMapping("upload")
    public R upload(@RequestParam MultipartFile file) throws IOException {
        QiniuConfig qiniuConfig = iQiniuConfigService.getById(1);
        if (qiniuConfig == null) {
            return R.error("七牛云配置不存在");
        }
        if (StringUtils.isEmpty(qiniuConfig.getAccessKey())) {
            return R.error("七牛云配置错误");
        }
        // 构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(QiniuUtil.getRegion(qiniuConfig.getRegion()));
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
        String upToken = auth.uploadToken(qiniuConfig.getBucket());
        String key = file.getOriginalFilename();
        Response response = uploadManager.put(file.getBytes(), key, upToken);

        DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
        //存入数据库
        QiniuContent qiniuContent = new QiniuContent();
        qiniuContent.setSuffix(FilenameUtils.getExtension(key));
        qiniuContent.setBucket(qiniuConfig.getBucket());
        if (qiniuConfig.getType() == 0) {
            qiniuContent.setType("公开");
        } else {
            qiniuContent.setType("私有");
        }
        qiniuContent.setName(FilenameUtils.getBaseName(key));
        qiniuContent.setUrl(qiniuConfig.getHost() + "/" + putRet.key);
        qiniuContent.setSize(FileUtils.getSize(Integer.parseInt(file.getSize() + "")));
        qiniuContent.setCreateTime(new Date());
        return R.result(iQiniuContentService.save(qiniuContent)).add("id", qiniuContent.getContentId()).add("fileUrl", qiniuContent.getUrl());
    }
}
