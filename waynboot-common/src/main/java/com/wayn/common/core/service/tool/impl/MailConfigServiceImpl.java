package com.wayn.common.core.service.tool.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.common.core.domain.tool.MailConfig;
import com.wayn.common.core.mapper.tool.MailConfigMapper;
import com.wayn.common.core.service.tool.IMailConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class MailConfigServiceImpl extends ServiceImpl<MailConfigMapper, MailConfig> implements IMailConfigService {

    @Override
    public boolean checkMailConfig(MailConfig mailConfig) {
        return !StringUtils.isEmpty(mailConfig.getFromUser())
                && !StringUtils.isEmpty(mailConfig.getHost())
                && !StringUtils.isEmpty(mailConfig.getPass());
    }
}
