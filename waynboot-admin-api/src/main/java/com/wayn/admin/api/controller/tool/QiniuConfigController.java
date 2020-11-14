package com.wayn.admin.api.controller.tool;


import com.wayn.common.core.domain.tool.QiniuConfig;
import com.wayn.common.core.service.tool.IQiniuConfigService;
import com.wayn.common.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("tool/qiniu")
public class QiniuConfigController {

    @Autowired
    private IQiniuConfigService iQiniuConfigService;

    @GetMapping
    public R info() {
        return R.success().add("data", iQiniuConfigService.getById(1));
    }

    @PutMapping
    public R update(@Valid @RequestBody QiniuConfig qiniuConfig) {
        qiniuConfig.setId(1L);
        iQiniuConfigService.updateById(qiniuConfig);
        return R.success("修改成功");
    }
}
