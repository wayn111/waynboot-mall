package com.wayn.admin.api.controller.tool;


import com.wayn.common.core.service.tool.IQiniuConfigService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("tool/qiniu")
public class QiniuConfigController {

    @Autowired
    private IQiniuConfigService iQiniuConfigService;

    @GetMapping
    public R info() {
        return R.success().add("data", iQiniuConfigService.getById(1));
    }
}
