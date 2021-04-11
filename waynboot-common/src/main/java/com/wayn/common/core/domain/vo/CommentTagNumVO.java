package com.wayn.common.core.domain.vo;

import lombok.Data;

@Data
public class CommentTagNumVO {

    /**
     * 好评数量
     */
    private Integer goodsNum;

    /**
     * 中评数量
     */
    private Integer middleNum;

    /**
     * 差评数量
     */
    private Integer badNum;

    /**
     * 总数
     */
    private Integer totalNum;

    /**
     * 有图数量
     */
    private Integer hasPictureNum;
}
