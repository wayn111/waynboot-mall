package com.wayn.data.elastic.manager;

import com.alibaba.fastjson.JSON;
import com.wayn.data.elastic.config.ElasticConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class ElasticDocument implements DisposableBean {

    private ElasticConfig elasticConfig;
    private RestHighLevelClient restHighLevelClient;

    /**
     * 创建索引
     *
     * @param idxName 缩影名称
     * @param idxSQL  索引创建语句
     */
    public boolean createIndex(String idxName, String idxSQL) throws IOException {
        if (indexExist(idxName)) {
            log.info("idxName={} 已经存在,idxSql={}", idxName, idxSQL);
            return false;
        }
        CreateIndexRequest request = new CreateIndexRequest(idxName);
        buildSetting(request);
        request.mapping(idxSQL, XContentType.JSON);
        // request.settings() 手工指定Setting
        CreateIndexResponse res = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        return res.isAcknowledged();
    }

    /**
     * 断某个index是否存在
     *
     * @param idxName 索引名称
     * @return boolean
     * @throws IOException 异常
     */
    public boolean indexExist(String idxName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(idxName);
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);
        // request.indicesOptions(IndicesOptions.lenientExpandOpen());
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 设置分片和副本
     *
     * @param request 创建索引请求
     */
    public void buildSetting(CreateIndexRequest request) {
        request.settings(Settings.builder().put("index.number_of_shards", elasticConfig.getShards())
                .put("index.number_of_replicas", elasticConfig.getReplicas()));
    }

    /**
     * @param idxName index
     * @param entity  对象
     */
    public boolean insertOrUpdateOne(String idxName, ElasticEntity entity) throws IOException {
        IndexRequest request = new IndexRequest(idxName);
        log.info("Data : id={},entity={}", entity.getId(), JSON.toJSONString(entity.getData()));
        request.id(entity.getId());
        request.source(entity.getData(), XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                log.error(failure.reason(), failure.getCause());
            }
        }
        return indexResponse.status().equals(RestStatus.OK);
    }


    /**
     * 批量插入数据
     *
     * @param idxName index
     * @param list    带插入列表
     */
    public boolean insertBatch(String idxName, List<ElasticEntity> list) throws IOException {
        BulkRequest request = new BulkRequest();
        list.forEach(item -> request.add(new IndexRequest(idxName).id(item.getId())
                .source(item.getData(), XContentType.JSON)));
        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        return bulkResponseHandler(bulkResponse);
    }

    /**
     * bulkResponse 处理
     *
     * @param bulkResponse 批量请求响应
     * @return boolean
     */
    private boolean bulkResponseHandler(BulkResponse bulkResponse) {
        boolean flag = true;
        for (BulkItemResponse response : bulkResponse) {
            if (response.isFailed()) {
                flag = false;
                BulkItemResponse.Failure failure = response.getFailure();
                log.error(failure.getMessage(), failure.getCause());
            }
        }
        return flag;
    }

    /**
     * 批量删除
     *
     * @param idxName index
     * @param idList  待删除列表
     */
    public <T> boolean deleteBatch(String idxName, Collection<T> idList) throws IOException {
        BulkRequest request = new BulkRequest();
        idList.forEach(item -> request.add(new DeleteRequest(idxName, item.toString())));
        BulkResponse bulk = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        return bulkResponseHandler(bulk);
    }

    /**
     * 删除文档
     *
     * @param idxName 索引名称
     * @param id      文档ID
     * @return boolean
     */
    public boolean delete(String idxName, String id) throws IOException {
        DeleteRequest request = new DeleteRequest(idxName, id);
        DeleteResponse deleteResponse = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                log.error(failure.reason());
            }
        }
        return deleteResponse.status().equals(RestStatus.OK);
    }

    /**
     * 搜索文档
     *
     * @param idxName index
     * @param builder 查询参数
     * @param c       结果类对象
     * @return java.util.List<T>
     */
    public <T> List<T> search(String idxName, SearchSourceBuilder builder, Class<T> c) throws IOException {
        SearchRequest request = new SearchRequest(idxName);
        request.source(builder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        return Arrays.stream(hits).map(hit -> JSON.parseObject(hit.getSourceAsString(), c)).collect(Collectors.toList());
    }


    /**
     * 在单个http请求中并行执行多个搜索请求
     *
     * @param request 多个搜索请求
     * @param c       结果类对象
     * @return java.util.List<T>
     */
    public <T> List<T> msearch(MultiSearchRequest request, Class<T> c) throws IOException {
        MultiSearchResponse response = restHighLevelClient.msearch(request, RequestOptions.DEFAULT);
        MultiSearchResponse.Item[] responseResponses = response.getResponses();
        List<T> all = new ArrayList<>();
        for (MultiSearchResponse.Item item : responseResponses) {
            SearchHits hits = item.getResponse().getHits();
            List<T> res = new ArrayList<>(hits.getHits().length);
            for (SearchHit hit : hits) {
                res.add(JSON.parseObject(hit.getSourceAsString(), c));
            }
            all.addAll(res);
        }
        return all;
    }

    /**
     * 删除index
     *
     * @param idxName 索引名称
     * @return boolean
     */
    public boolean deleteIndex(String idxName) throws IOException {
        if (!this.indexExist(idxName)) {
            log.error(" idxName={} 不存在", idxName);
            return false;
        }
        AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices()
                .delete(new DeleteIndexRequest(idxName), RequestOptions.DEFAULT);
        return acknowledgedResponse.isAcknowledged();
    }


    /**
     * 根据查询条件删除文档
     *
     * @param idxName 缩影名称
     * @param builder 查询条件
     * @return boolean
     */
    public boolean deleteByQuery(String idxName, QueryBuilder builder) throws IOException {
        DeleteByQueryRequest request = new DeleteByQueryRequest(idxName);
        request.setQuery(builder);
        // 设置批量操作数量,最大为10000
        request.setBatchSize(100);
        // 版本冲突时继续执行
        request.setConflicts("proceed");
        BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
        List<BulkItemResponse.Failure> bulkFailures = bulkByScrollResponse.getBulkFailures();
        boolean flag = true;
        for (BulkItemResponse.Failure bulkFailure : bulkFailures) {
            log.error(bulkFailure.getMessage(), bulkFailure.getCause());
            flag = false;
        }
        return flag;
    }

    /**
     * bean对象生命周期结束时，关闭连接
     */
    @Override
    public void destroy() {
        try {
            if (restHighLevelClient != null) {
                restHighLevelClient.close();
            }
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }
}
