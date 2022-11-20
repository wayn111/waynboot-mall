package com.wayn.common.enums.domain;

import java.util.Arrays;
import java.util.List;

public class StatusConverter extends AbstractIntConverter {

    @Override
    List<ConverterDTO> getArr() {
        StatusEnum[] values = StatusEnum.values();
        return Arrays.stream(values).map(sexEnum -> new ConverterDTO(sexEnum.getType(), sexEnum.getDesc())).toList();
    }

    enum StatusEnum {

        MAN(0, "启用"),
        WOMAN(1, "禁用");


        private Integer type;
        private String desc;

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
