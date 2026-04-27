package com.wayn.common.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 搜索历史保存请求。
 */
@Data
public class SearchHistorySaveReqVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6592270117658693816L;

    private String keyword;
    private String from;
    private Boolean hasGoods;
}
