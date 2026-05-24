package com.wayn.domain.goods.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wayn.domain.api.goods.entity.Goods;
import com.wayn.domain.api.goods.mapper.GoodsMapper;
import com.wayn.data.elastic.constant.EsConstants;
import com.wayn.data.elastic.manager.ElasticDocument;
import com.wayn.data.elastic.manager.ElasticEntity;
import com.wayn.data.redis.constant.RedisKeyEnum;
import com.wayn.data.redis.manager.RedisLock;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.util.file.FileUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 商品 ES 同步支撑服务。
 * 集中处理商品索引重建、单商品文档同步和索引级锁控制，避免商品写路径与搜索索引逻辑耦合。
 */
@Service
@AllArgsConstructor
public class GoodsElasticSyncSupport {

    private final GoodsMapper goodsMapper;
    private final ElasticDocument elasticDocument;
    private final RedisLock redisLock;

    /**
     * 全量重建商品索引并同步所有商品文档。
     * 该方法使用 Redis 锁串行化索引重建，避免多个实例同时删索引和重建索引。
     *
     * @return 同步结果
     */
    public boolean syncGoodsToEs() {
        boolean lock = redisLock.lock(RedisKeyEnum.ES_SYNC_CACHE.getKey(), 2);
        if (!lock) {
            throw new BusinessException("加锁失败");
        }
        try {
            try {
                elasticDocument.deleteIndex(EsConstants.ES_GOODS_INDEX);
                if (!elasticDocument.createIndex(EsConstants.ES_GOODS_INDEX, loadGoodsIndexConfig())) {
                    return false;
                }
            } catch (IOException e) {
                throw new BusinessException("商品同步 ES 失败");
            }
            List<ElasticEntity> entities = goodsMapper.selectList(Wrappers.lambdaQuery(Goods.class)).stream()
                    .map(this::buildGoodsElasticEntity)
                    .toList();
            try {
                return elasticDocument.insertBatch(EsConstants.ES_GOODS_INDEX, entities);
            } catch (IOException e) {
                throw new BusinessException("商品同步 ES 失败");
            }
        } finally {
            redisLock.unLock(RedisKeyEnum.ES_SYNC_CACHE.getKey());
        }
    }

    /**
     * 同步单个商品到 ES。
     *
     * @param goods 商品信息
     * @return 同步结果
     * @throws IOException 写入 ES 时的 IO 异常
     */
    public boolean syncGoods2Es(Goods goods) throws IOException {
        ElasticEntity elasticEntity = buildGoodsElasticEntity(goods);
        if (!elasticDocument.insertOrUpdateOne(EsConstants.ES_GOODS_INDEX, elasticEntity)) {
            throw new BusinessException("商品同步 ES 失败");
        }
        return true;
    }

    /**
     * 从 ES 中删除单个商品文档。
     *
     * @param goodsId 商品 ID
     * @throws IOException 删除 ES 文档时的 IO 异常
     */
    public void deleteGoodsFromEs(Long goodsId) throws IOException {
        elasticDocument.delete(EsConstants.ES_GOODS_INDEX, goodsId.toString());
    }

    /**
     * 加载商品索引配置文件。
     *
     * @return 索引配置 JSON
     */
    private String loadGoodsIndexConfig() {
        try (InputStream inputStream = this.getClass().getResourceAsStream(EsConstants.ES_INDEX_GOODS_FILENAME)) {
            if (inputStream == null) {
                throw new BusinessException("ES 索引配置不存在");
            }
            return FileUtils.getContent(inputStream);
        } catch (IOException e) {
            throw new BusinessException("读取 ES 索引配置失败");
        }
    }

    /**
     * 构建 ES 商品文档。
     *
     * @param goods 商品信息
     * @return ES 文档对象
     */
    private ElasticEntity buildGoodsElasticEntity(Goods goods) {
        ElasticEntity elasticEntity = new ElasticEntity();
        elasticEntity.setId(goods.getId().toString());
        Map<String, Object> map = new HashMap<>();
        map.put("id", goods.getId());
        map.put("name", goods.getName());
        map.put("pyname", goods.getName());
        map.put("sales", goods.getVirtualSales());
        map.put("isHot", goods.getIsHot());
        map.put("isNew", goods.getIsNew());
        map.put("countPrice", goods.getCounterPrice());
        map.put("retailPrice", goods.getRetailPrice());
        map.put("keyword", resolveKeywords(goods.getKeywords()));
        map.put("isOnSale", goods.getIsOnSale());
        map.put("createTime", goods.getCreateTime());
        elasticEntity.setData(map);
        return elasticEntity;
    }

    /**
     * 解析商品关键字列表。
     *
     * @param keywords 逗号分隔关键字
     * @return 去空白后的关键字集合
     */
    private List<String> resolveKeywords(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(keywords.split(","))
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .toList();
    }
}
