package com.wayn.common.enums.domain;

import java.util.Arrays;
import java.util.List;

/**
 * 通用状态字段转换类
 */
public class StatusConverter extends AbstractIntConverter {

    @Override
    List<ConverterDTO> getArr() {
        StatusEnum[] values = StatusEnum.values();
        return Arrays.stream(values).map(sexEnum -> new ConverterDTO(sexEnum.getType(), sexEnum.getDesc())).toList();
    }

    /**
     * 状态枚举
     */
    enum StatusEnum {

        MAN(0, "启用"),
        WOMAN(1, "禁用");


        private final Integer type;
        private final String desc;

        StatusEnum(Integer type, String desc) {
            this.type = type;
            this.desc = desc;
        }


        public Integer getType() {
            return type;
        }

        public String getDesc() {
            return desc;
        }

    }
}
