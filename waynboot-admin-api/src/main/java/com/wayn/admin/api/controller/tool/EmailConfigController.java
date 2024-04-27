package com.wayn.admin.api.controller.tool;

import cn.hutool.core.bean.BeanUtil;
import com.wayn.common.core.entity.tool.EmailConfig;
import com.wayn.common.core.vo.EmailConfigVO;
import com.wayn.common.core.vo.SendMailVO;
import com.wayn.common.core.service.tool.IMailConfigService;
import com.wayn.util.enums.ReturnCodeEnum;
import com.wayn.util.util.R;
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

    /**
     * 获取邮件配置
     *
     * @return
     */
    @PreAuthorize("@ss.hasPermi('tool:email:info')")
    @GetMapping
    public R<EmailConfig> info() {
        return R.success(mailConfigService.getById(1));
    }

    /**
     * 更新邮件配置
     *
     * @param emailConfig
     * @return
     */
    @PreAuthorize("@ss.hasPermi('tool:email:update')")
    @PutMapping
    public R<Boolean> update(@Valid @RequestBody EmailConfigVO emailConfig) {
        emailConfig.setId(1L);
        mailConfigService.saveOrUpdate(BeanUtil.copyProperties(emailConfig, EmailConfig.class));
        return R.success();
    }

    /**
     * 发送邮件
     *
     * @param mailVO
     * @return
     */
    @PreAuthorize("@ss.hasPermi('tool:email:send')")
    @PostMapping("send")
    public R<Boolean> sendMail(@Valid @RequestBody SendMailVO mailVO) {
        EmailConfig emailConfig = mailConfigService.getById(1L);
        if (!mailConfigService.checkMailConfig(emailConfig)) {
            return R.error(ReturnCodeEnum.TOOL_EMAIL_ERROR);
        }
        MailUtil.sendMail(emailConfig, mailVO, false);
        return R.success();
    }
}
