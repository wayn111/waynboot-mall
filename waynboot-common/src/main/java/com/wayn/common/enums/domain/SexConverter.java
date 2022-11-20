package com.wayn.common.enums.domain;


import java.util.Arrays;
import java.util.List;

public class SexConverter extends AbstractIntConverter {

    @Override
    List<ConverterDTO> getArr() {
        SexEnum[] values = SexEnum.values();
        return Arrays.stream(values).map(sexEnum -> new ConverterDTO(sexEnum.getType(), sexEnum.getDesc())).toList();
    }

    enum SexEnum {
        MAN(0, "男"),
        WOMAN(1, "女");


        private Integer type;
        private String desc;

        SexEnum(Integer type, String desc) {
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
