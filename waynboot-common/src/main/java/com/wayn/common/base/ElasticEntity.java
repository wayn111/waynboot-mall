package com.wayn.common.base;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ElasticEntity {

    private String id;

    private Map<String, Object> data = new HashMap<>();
}
