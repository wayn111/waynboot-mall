package com.wayn.common.core.service.tool;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.common.core.domain.tool.MailConfig;

public interface IMailConfigService extends IService<MailConfig> {

    boolean checkMailConfig(MailConfig mailConfig);
}
