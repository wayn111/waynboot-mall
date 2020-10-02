package com.wayn.admin.api.controller.tool;

import com.wayn.common.core.domain.tool.EmailConfig;
import com.wayn.common.core.domain.vo.SendMailVO;
import com.wayn.common.core.service.tool.IMailConfigService;
import com.wayn.common.util.R;
import com.wayn.common.util.mail.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("tool/email")
public class EmailConfigController {
    @Autowired
    private IMailConfigService mailConfigService;

    @GetMapping
    public R info() {
        return R.success().add("data", mailConfigService.getById(1));
    }

    @PutMapping
    public R update(@Valid @RequestBody EmailConfig emailConfig) {
        emailConfig.setId(1L);
        mailConfigService.updateById(emailConfig);
        return R.success("修改成功");
    }

    @PostMapping("send")
    public R sendMail(@Valid @RequestBody SendMailVO mailVO) {
        EmailConfig emailConfig = mailConfigService.getById(1L);
        if (!mailConfigService.checkMailConfig(emailConfig)) {
            return R.error("邮件信息未配置完全，请先填写配置信息");
        }
        MailUtil.sendMail(emailConfig, mailVO, false);
        return R.success("发送成功，请等待");
    }
}
