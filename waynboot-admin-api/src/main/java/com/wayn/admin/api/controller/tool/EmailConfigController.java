package com.wayn.admin.api.controller.tool;

import com.wayn.common.core.domain.tool.EmailConfig;
import com.wayn.common.core.domain.vo.SendMailVO;
import com.wayn.common.core.service.tool.IMailConfigService;
import com.wayn.common.enums.ReturnCodeEnum;
import com.wayn.common.util.R;
import com.wayn.common.util.mail.MailUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("tool/email")
public class EmailConfigController {
    private IMailConfigService mailConfigService;

    @GetMapping
    public R info() {
        return R.success().add("data", mailConfigService.getById(1));
    }

    @PutMapping
    public R update(@Valid @RequestBody EmailConfig emailConfig) {
        emailConfig.setId(1L);
        mailConfigService.updateById(emailConfig);
        return R.success();
    }

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
