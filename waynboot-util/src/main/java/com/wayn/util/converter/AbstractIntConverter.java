package com.wayn.util.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * 定义int字段抽象转换类，实现通用转换逻辑
 */
public abstract class AbstractIntConverter implements Converter<Integer> {
    abstract List<ConverterDTO> getArr();

    public WriteCellData<?> convertToExcelData(Integer value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        List<ConverterDTO> values = getArr();
        Map<Integer, String> map = values.stream().collect(toMap(ConverterDTO::getType, ConverterDTO::getDesc));
        String result = map.getOrDefault(value, "");
        return new WriteCellData<>(result);
    }

    @Setter
    @Getter
    static class ConverterDTO {
        private Integer type;
        private String desc;

        public ConverterDTO(Integer type, String desc) {
            this.type = type;
            this.desc = desc;
        }
    }
}
