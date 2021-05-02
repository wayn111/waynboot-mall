package com.wayn.common.core.domain.vo;

import lombok.Data;

@Data
public class Base64FileVO {

    /**
     * 文件名称
     */
    String filename;

    /**
     * base64文件字符内容
     */
    String base64content;
}
