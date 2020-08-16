package com.wayn.admin.api.controller.tool;

import com.wayn.common.core.domain.tool.MailConfig;
import com.wayn.common.core.domain.vo.SendMailVO;
import com.wayn.common.core.service.tool.IMailConfigService;
import com.wayn.common.util.R;
import com.wayn.common.util.mail.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tool/mailConfig")
public class MallConfigController {
    @Autowired
    private IMailConfigService mailConfigService;

    @GetMapping("{id}")
    public R info(@PathVariable Long id) {
        return R.success().add("data", mailConfigService.getById(id));
    }

    @PostMapping("update")
    public R update(MailConfig mailConfig) {
        mailConfig.setId(1L);
        mailConfigService.updateById(mailConfig);
        return R.success("修改成功");
    }

    @PostMapping("sendMail")
    public R sendMail(SendMailVO mailVO) {
        MailConfig mailConfig = mailConfigService.getById(1L);
        if (!mailConfigService.checkMailConfig(mailConfig)) {
            return R.error("邮件信息未配置完全，请先填写配置信息");
        }
        MailUtil.sendMail(mailConfig, mailVO, false);
        return R.success("发送成功，请等待");
    }
}
