package com.wayn.admin.api.controller.tool;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiniu.common.QiniuException;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.core.domain.tool.QiniuConfig;
import com.wayn.common.core.domain.tool.QiniuContent;
import com.wayn.common.core.domain.vo.QiniuConfigVO;
import com.wayn.common.core.service.tool.IQiniuConfigService;
import com.wayn.common.core.service.tool.IQiniuContentService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 七牛云配置
 *
 * @author wayn
 * @since 2020-07-21
 */
@RestController
@AllArgsConstructor
@RequestMapping("tool/qiniu")
public class QiniuController extends BaseController {

    private IQiniuConfigService iQiniuConfigService;

    private IQiniuContentService iQiniuContentService;

    @PreAuthorize("@ss.hasPermi('tool:qiniu:list')")
    @GetMapping("/list")
    public R list(QiniuContent qiniuContent) {
        Page<QiniuContent> page = getPage();
        return R.success().add("page", iQiniuContentService.listPage(page, qiniuContent));
    }

    @PreAuthorize("@ss.hasPermi('tool:qiniu:info')")
    @GetMapping("config")
    public R info() {
        return R.success().add("data", iQiniuConfigService.getById(1));
    }

    @PreAuthorize("@ss.hasPermi('tool:qiniu:update')")
    @PutMapping("config")
    public R update(@Valid @RequestBody QiniuConfigVO qiniuConfigVO) {
        qiniuConfigVO.setId(1L);
        QiniuConfig qiniuConfig = BeanUtil.copyProperties(qiniuConfigVO, QiniuConfig.class);
        iQiniuConfigService.saveOrUpdate(qiniuConfig);
        return R.success();
    }

    @PreAuthorize("@ss.hasPermi('tool:qiniu:upload')")
    @PostMapping("upload")
    public R upload(@RequestParam MultipartFile file) throws IOException {
        QiniuConfig qiniuConfig = iQiniuConfigService.getById(1);
        if (qiniuConfig == null) {
            return R.error(ReturnCodeEnum.TOOL_QINIU_NOT_EXISTS_ERROR);
        }
        if (StringUtils.isEmpty(qiniuConfig.getAccessKey())) {
            return R.error(ReturnCodeEnum.TOOL_QINIU_CONFIG_ERROR);
        }
        QiniuContent qiniuContent = iQiniuContentService.upload(file, qiniuConfig);
        return R.result(iQiniuContentService.save(qiniuContent)).add("id", qiniuContent.getContentId()).add("fileUrl", qiniuContent.getUrl());
    }

    @PreAuthorize("@ss.hasPermi('tool:qiniu:download')")
    @GetMapping("download/{id}")
    public R download(@PathVariable Long id) {
        QiniuConfig qiniuConfig = iQiniuConfigService.getById(1);
        if (qiniuConfig == null) {
            return R.error(ReturnCodeEnum.TOOL_QINIU_NOT_EXISTS_ERROR);
        }
        if (StringUtils.isEmpty(qiniuConfig.getAccessKey())) {
            return R.error(ReturnCodeEnum.TOOL_QINIU_CONFIG_ERROR);
        }
        return R.success().add("url", iQiniuContentService.download(id, qiniuConfig));
    }

    @PreAuthorize("@ss.hasPermi('tool:qiniu:syncQiniu')")
    @GetMapping("syncQiniu")
    public R syncQiniu() {
        QiniuConfig qiniuConfig = iQiniuConfigService.getById(1);
        if (qiniuConfig == null) {
            return R.error(ReturnCodeEnum.TOOL_QINIU_NOT_EXISTS_ERROR);
        }
        if (StringUtils.isEmpty(qiniuConfig.getAccessKey())) {
            return R.error(ReturnCodeEnum.TOOL_QINIU_CONFIG_ERROR);
        }
        iQiniuContentService.syncQiniu(qiniuConfig);
        return R.success();
    }

    @PreAuthorize("@ss.hasPermi('tool:qiniu:delete')")
    @DeleteMapping(value = "{id}")
    public R delete(@PathVariable Long id) throws QiniuException {
        QiniuConfig qiniuConfig = iQiniuConfigService.getById(1);
        if (qiniuConfig == null) {
            return R.error(ReturnCodeEnum.TOOL_QINIU_NOT_EXISTS_ERROR);
        }
        if (StringUtils.isEmpty(qiniuConfig.getAccessKey())) {
            return R.error(ReturnCodeEnum.TOOL_QINIU_CONFIG_ERROR);
        }
        return R.result(iQiniuContentService.delete(id, qiniuConfig));
    }
}
