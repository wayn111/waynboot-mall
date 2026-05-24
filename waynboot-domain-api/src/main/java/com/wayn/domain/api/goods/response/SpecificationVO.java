package com.wayn.domain.api.goods.response;

import com.wayn.domain.api.goods.entity.GoodsSpecification;
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
