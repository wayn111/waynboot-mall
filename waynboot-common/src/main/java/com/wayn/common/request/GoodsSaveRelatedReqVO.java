package com.wayn.common.request;

import com.wayn.common.core.vo.GoodsAttributeVO;
import com.wayn.common.core.vo.GoodsProductVO;
import com.wayn.common.core.vo.GoodsSpecificationVO;
import com.wayn.common.core.vo.GoodsVO;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class GoodsSaveRelatedReqVO {

    @Valid
    GoodsVO goods;
    @Valid
    GoodsSpecificationVO[] specifications;
    @Valid
    GoodsAttributeVO[] attributes;
    @Valid
    GoodsProductVO[] products;
}
