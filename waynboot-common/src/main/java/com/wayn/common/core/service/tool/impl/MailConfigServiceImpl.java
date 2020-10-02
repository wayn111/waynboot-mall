package com.wayn.common.core.service.tool.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.tool.EmailConfig;
import com.wayn.common.core.mapper.tool.MailConfigMapper;
import com.wayn.common.core.service.tool.IMailConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class MailConfigServiceImpl extends ServiceImpl<MailConfigMapper, EmailConfig> implements IMailConfigService {

    @Override
    public boolean checkMailConfig(EmailConfig emailConfig) {
        return !StringUtils.isEmpty(emailConfig.getFromUser())
                && !StringUtils.isEmpty(emailConfig.getHost())
                && !StringUtils.isEmpty(emailConfig.getPass());
    }
}
