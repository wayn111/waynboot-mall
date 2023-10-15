package com.wayn.admin.api.controller.tool;

import cn.hutool.core.bean.BeanUtil;
import com.wayn.common.core.domain.tool.EmailConfig;
import com.wayn.common.core.domain.vo.EmailConfigVO;
import com.wayn.common.core.domain.vo.SendMailVO;
import com.wayn.common.core.service.tool.IMailConfigService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import com.wayn.common.util.mail.MailUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件配置
 *
 * @author wayn
 * @since 2020-07-21
 */
@RestController
@AllArgsConstructor
@RequestMapping("tool/email")
public class EmailConfigController {
    private IMailConfigService mailConfigService;

    @PreAuthorize("@ss.hasPermi('tool:email:info')")
    @GetMapping
    public R info() {
        return R.success().add("data", mailConfigService.getById(1));
    }

    @PreAuthorize("@ss.hasPermi('tool:email:update')")
    @PutMapping
    public R update(@Valid @RequestBody EmailConfigVO emailConfig) {
        emailConfig.setId(1L);
        mailConfigService.saveOrUpdate(BeanUtil.copyProperties(emailConfig, EmailConfig.class));
        return R.success();
    }

    @PreAuthorize("@ss.hasPermi('tool:email:send')")
    @PostMapping("send")
    public R sendMail(@Valid @RequestBody SendMailVO mailVO) {
        EmailConfig emailConfig = mailConfigService.getById(1L);
        if (!mailConfigService.checkMailConfig(emailConfig)) {
            return R.error(ReturnCodeEnum.TOOL_EMAIL_ERROR);
        }
        MailUtil.sendMail(emailConfig, mailVO, false, false);
        return R.success();
    }
}
