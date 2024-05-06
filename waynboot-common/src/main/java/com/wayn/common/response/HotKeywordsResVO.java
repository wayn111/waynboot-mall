package com.wayn.common.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author: waynaqua
 * @date: 2024/4/27 16:36
 */
@Data
public class HotKeywordsResVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5468848317089294933L;

    /**
     * 热词列表
     */
    private List<String> hotStrings;
    /**
     * 默认搜索
     */
    private String defaultSearch;
}
