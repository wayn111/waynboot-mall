package com.wayn.common.model.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 搜索历史响应。
 */
@Data
public class SearchHistoryResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1967795515875430080L;

    private Long id;
    private String keyword;
    private String from;
    private Boolean hasGoods;
    private LocalDateTime createTime;
}
