package com.wayn.common.core.domain.shop.vo;

import com.wayn.common.core.domain.shop.GoodsSpecification;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SpecificationVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 950869220112646783L;
    private String name;
    private List<GoodsSpecification> valueList;

}
