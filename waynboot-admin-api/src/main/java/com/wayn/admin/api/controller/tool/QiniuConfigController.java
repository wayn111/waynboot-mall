package com.wayn.admin.api.controller.tool;


import com.wayn.admin.framework.config.WaynConfig;
import com.wayn.admin.framework.manager.upload.service.UploadService;
import com.wayn.common.core.domain.tool.QiniuConfig;
import com.wayn.common.core.service.tool.IQiniuConfigService;
import com.wayn.common.util.R;
import com.wayn.common.util.file.FileUploadUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("tool/qiniu")
public class QiniuConfigController {

    @Autowired
    private IQiniuConfigService iQiniuConfigService;

    @Autowired
    private UploadService uploadService;

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

    @PostMapping
    public R upload(@RequestParam MultipartFile file) throws IOException {
        QiniuConfig qiniuConfig = iQiniuConfigService.getById(1);
        if (qiniuConfig == null) {
            return R.error("七牛云配置不存");
        }
        if (StringUtils.isEmpty(qiniuConfig.getAccessKey())) {
            return R.error("七牛云配置错误");
        }
        // 上传文件路径
        String filePath = WaynConfig.getUploadDir();
        String fileName = FileUploadUtil.uploadFile(file, filePath);
        String fileUrl = uploadService.qiniuUploadFile(fileName);
        return R.success().add("fileUrl", fileUrl);
    }
}
