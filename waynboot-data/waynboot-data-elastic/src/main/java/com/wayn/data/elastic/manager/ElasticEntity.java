package com.wayn.data.elastic.manager;

import lombok.Data;

import java.util.Map;

/**
 * elastic存储对象
 */
@Data
public class ElasticEntity {

    private String id;

    private Map<String, Object> data;
}
