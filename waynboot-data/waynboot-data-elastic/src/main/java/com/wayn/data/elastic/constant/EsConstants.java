package com.wayn.data.elastic.constant;

public class EsConstants {

    /**
     * 商品的索引名称
     */
    public static final String ES_GOODS_INDEX = "goods";
    /**
     * 商品索引创建缓存，创建成功后删除
     */
    public static final String ES_GOODS_INDEX_KEY = "es_goods";
    /**
     * es商品索引的创建语句所在文件位置
     */
    public static String ES_INDEX_GOODS_FILENAME = "/es/index/goods.json";

}
