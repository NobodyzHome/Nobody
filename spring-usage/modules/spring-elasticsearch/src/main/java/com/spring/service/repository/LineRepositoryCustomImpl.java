package com.spring.service.repository;

import com.alibaba.fastjson.JSON;
import com.spring.data.domain.BaseLine;
import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ScrolledPage;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class LineRepositoryCustomImpl implements LineRepositoryCustom {

    private static Logger logger = LoggerFactory.getLogger(LineRepositoryCustomImpl.class);

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public boolean createIndex(Class clazz) {
        if (elasticsearchTemplate.indexExists(clazz)) {
            elasticsearchTemplate.deleteIndex(clazz);
        }
        boolean indexCreateSuccess = elasticsearchTemplate.createIndex(clazz);
        boolean mappingCreateSuccess = elasticsearchTemplate.putMapping(clazz);
        return indexCreateSuccess && mappingCreateSuccess;
    }

    @Override
    public List<BaseLine> scrollAll(QueryBuilder queryBuilder) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(PageRequest.of(0, 100)).withQuery(queryBuilder)
                .withScriptField(new ScriptField("isUpdated", new Script("doc.updateTime.date.isAfter(doc.createTime.date)")))
                .withScriptField(new ScriptField("multiLineNo", new Script("2 * doc.lineNo.value")))
                .withSourceFilter(new FetchSourceFilterBuilder().withIncludes("*").build()).build();

        ScrolledPage<BaseLine> baseLinePage = (ScrolledPage) elasticsearchTemplate.startScroll(TimeUnit.MINUTES.toMillis(2), searchQuery, BaseLine.class);
        List<BaseLine> baseLineList = new ArrayList<>((int) baseLinePage.getTotalElements());
        do {
            logger.info("当前获取到的数据：{}，总共数据条数：{}，获取到的数据：{}", baseLinePage.getNumberOfElements(), baseLinePage.getTotalElements(), JSON.toJSONString(baseLinePage.getContent()));
            baseLineList.addAll(baseLinePage.getContent());

            String scrollId = baseLinePage.getScrollId();
            baseLinePage = (ScrolledPage) elasticsearchTemplate.continueScroll(scrollId, TimeUnit.MINUTES.toMillis(2), BaseLine.class);
        } while (!baseLinePage.getContent().isEmpty());

        return baseLineList;
    }

    @Override
    public List<BaseLine> searchAfterAll(QueryBuilder queryBuilder) {
        Client client = elasticsearchTemplate.getClient();
        Object[] sortValues = null;
        List<BaseLine> baseLineList = new ArrayList<>();
        DefaultResultMapper resultMapper = new DefaultResultMapper();
        do {
            SearchRequestBuilder searchRequestBuilder = client.prepareSearch("index_line").setTypes("line").setQuery(queryBuilder).setSize(30).addSort("_id", SortOrder.DESC);
            Optional.ofNullable(sortValues).ifPresent(searchRequestBuilder::searchAfter);

            SearchResponse searchResponse = searchRequestBuilder.get();
            SearchHit[] hits = searchResponse.getHits().getHits();
            logger.info("已获取{}个文档，共{}个文档，共花费{}!", hits.length, searchResponse.getHits().getTotalHits(), searchResponse.getTook());

            if (ArrayUtils.isEmpty(hits)) {
                break;
            } else {
                for (int i = 0, length = hits.length; i < length; i++) {
                    SearchHit hit = hits[i];
                    BaseLine baseLine = resultMapper.mapEntity(hit.getSourceAsString(), BaseLine.class);
                    baseLineList.add(baseLine);

                    if (i == length - 1) {
                        sortValues = hit.getSortValues();
                    }
                }
            }
        } while (true);

        return baseLineList;
    }

    @Override
    public Map<String, Integer> histogramByLineNo(QueryBuilder queryBuilder) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder).
                addAggregation(AggregationBuilders.histogram("groupByLineNo").field("lineNo").interval(50).missing(1111).minDocCount(0)).build();

        AggregatedPage<BaseLine> baseLinePage = elasticsearchTemplate.queryForPage(nativeSearchQuery, BaseLine.class);
        Histogram aggregation = baseLinePage.getAggregations().get("groupByLineNo");
        Map<String, Integer> result = new TreeMap<>(Comparator.comparingDouble(Double::valueOf));

        aggregation.getBuckets().forEach(bucket -> result.put(bucket.getKeyAsString(), Long.valueOf(bucket.getDocCount()).intValue()));
        return result;
    }

    @Override
    public Map<Date, Integer> dateHistogramByUpdateTime(QueryBuilder queryBuilder) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder).addAggregation(AggregationBuilders.dateHistogram("groupByUpdateTime")
                .field("updateTime").interval(TimeUnit.HOURS.toMillis(2)).format("yyyy-MM-dd HH").minDocCount(10).timeZone(DateTimeZone.forOffsetHours(8))).build();

        AggregatedPage<BaseLine> baseLinePage = elasticsearchTemplate.queryForPage(searchQuery, BaseLine.class);
        Map<Date, Integer> result = new TreeMap<>(Comparator.comparingLong(Date::getTime));

        Histogram dateHistogram = baseLinePage.getAggregations().get("groupByUpdateTime");
        dateHistogram.getBuckets().forEach(bucket -> {
            DateTime key = (DateTime) bucket.getKey();
            String keyAsString = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            logger.info("当前桶的key是：{},key_as_string是：{},doc_count是：{}", key, keyAsString, docCount);
            result.put(new Date(key.getMillis()), Long.valueOf(docCount).intValue());
        });
        return result;
    }
}
