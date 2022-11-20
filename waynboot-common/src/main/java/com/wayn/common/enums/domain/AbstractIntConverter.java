package com.wayn.common.enums.domain;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public abstract class AbstractIntConverter implements Converter<Integer> {
    abstract List<ConverterDTO> getArr();

    public WriteCellData<?> convertToExcelData(Integer value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        List<ConverterDTO> values = getArr();
        Map<Integer, String> map = values.stream().collect(toMap(ConverterDTO::getType, ConverterDTO::getDesc));
        String result = map.getOrDefault(value, "");
        return new WriteCellData<>(result);
    }

    static class ConverterDTO {
        private Integer type;
        private String desc;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public ConverterDTO(Integer type, String desc) {
            this.type = type;
            this.desc = desc;
        }
    }
}
