package com.learn.elasticsearch.demo.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.learn.elasticsearch.demo.service.ElasticsearchService;
import com.learn.model.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * description......
 *
 * @author yeeee
 * @since 2022/4/25 22:21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

    private final RestHighLevelClient restClient;

    @Override
    public boolean createIndex(String indexName, String mapping) throws IOException {

        // 创建索引
        CreateIndexRequest indexRequest = new CreateIndexRequest(indexName);
        restClient.indices().create(indexRequest, RequestOptions.DEFAULT);

        // 构建一个Index（索引）
        PutMappingRequest putMappingRequest = new PutMappingRequest(indexName).source(mapping, XContentType.JSON);

        // 同步执行
        AcknowledgedResponse acknowledgedResponse = restClient.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
        boolean acknowledged = acknowledgedResponse.isAcknowledged();
        if (!acknowledged) {
            log.info("index create error");
            return false;
        }
        return true;
    }

    @Override
    public boolean exists(String indexName) throws Exception {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return restClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    @Override
    public long count(QueryBuilder queryBuilder, String... indices) throws Exception {
        CountRequest countRequest = new CountRequest(indices);
        countRequest.query(queryBuilder);
        CountResponse countResponse = restClient.count(countRequest, RequestOptions.DEFAULT);
        return countResponse.getCount();
    }

    @Override
    public void delete(String indexName) throws Exception {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        restClient.indices().delete(request, RequestOptions.DEFAULT);
    }

    @Override
    public BulkResponse bulk(String index, List<Map<String, Object>> list) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (Map<String, Object> map : list) {
            String jsonStr = JSONUtil.toJsonStr(map);
            bulkRequest.add(new IndexRequest(index).id(map.get("id").toString()).source(JSONObject.parseObject(jsonStr)));
        }
        return restClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    @Override
    public PageVO<SearchHit> pageSearch(Integer pageNum, Integer pageSize, QueryBuilder queryBuilder, String... index) throws IOException {

        PageVO<SearchHit> pageVO = new PageVO<>(pageNum, pageSize);

        // 构造条件查询对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 查询条件
        searchSourceBuilder.query(queryBuilder);
        // 分页
        searchSourceBuilder.from((pageNum - 1) * pageSize).size(pageSize);
        // 排序
        searchSourceBuilder.sort(new FieldSortBuilder("id").order(SortOrder.DESC));

        // 构造搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restClient.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits hits = response.getHits();
        pageVO.setResult(Arrays.asList(hits.getHits()));
        pageVO.setTotal(hits.getTotalHits().value);

        return pageVO;
    }

    @Override
    public Aggregations aggregationSearch(AggregationBuilder aggregationBuilder, String... index) throws IOException {
        // 构造条件查询对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 查询条件
        searchSourceBuilder.aggregation(aggregationBuilder);
        // 构造搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restClient.search(searchRequest, RequestOptions.DEFAULT);
        return response.getAggregations();
    }
}
