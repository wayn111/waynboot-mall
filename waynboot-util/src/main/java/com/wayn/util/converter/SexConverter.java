package com.wayn.util.converter;


import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 通用性别字段转换类
 */
public class SexConverter extends AbstractIntConverter {

    @Override
    List<ConverterDTO> getArr() {
        SexEnum[] values = SexEnum.values();
        return Arrays.stream(values).map(sexEnum -> new ConverterDTO(sexEnum.getType(), sexEnum.getDesc())).toList();
    }

    /**
     * 性别枚举
     */
    @Getter
    enum SexEnum {
        MAN(0, "男"),
        WOMAN(1, "女");


        private final Integer type;
        private final String desc;

        SexEnum(Integer type, String desc) {
            this.type = type;
            this.desc = desc;
        }

    }

}
