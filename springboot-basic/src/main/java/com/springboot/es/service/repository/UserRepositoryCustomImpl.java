package com.springboot.es.service.repository;

import com.springboot.data.domain.BaseUser;
import com.springboot.data.domain.generate.DomainGenerator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ScrolledPage;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private static Logger logger = LoggerFactory.getLogger(UserRepositoryCustomImpl.class);

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public Page<BaseUser> findWithScripted(QueryBuilder queryBuilder, Pageable pageable) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder).withPageable(pageable)
                .withScriptField(new ScriptField("isUpdated", new Script("doc.updateTime.date.isAfter(doc.createTime.date)")))
                .withScriptField(new ScriptField("full", new Script("doc.firstName.value+'-'+doc.secondName.value")))
                .withScriptField(new ScriptField("isOld", new Script("doc.age.value >20")))
                .withSourceFilter(new FetchSourceFilterBuilder().withIncludes("*").build()).build();

        AggregatedPage<BaseUser> baseUsers = elasticsearchTemplate.queryForPage(searchQuery, BaseUser.class);
        return baseUsers;
    }

    @Override
    public boolean createIndex() {
        if (elasticsearchTemplate.indexExists(BaseUser.class)) {
            elasticsearchTemplate.deleteIndex(BaseUser.class);
        }

        boolean indexCreate = elasticsearchTemplate.createIndex(BaseUser.class);
        boolean mappingCreate = elasticsearchTemplate.putMapping(BaseUser.class);
        return indexCreate && mappingCreate;
    }

    @Override
    public List<String> indexDatas(int count) {
        List<BaseUser> baseUsers = DomainGenerator.generateUsers(count, true);
        List<String> idIndexed = baseUsers.stream().map(baseUser -> new IndexQueryBuilder().withObject(baseUser).build())
                .map(elasticsearchTemplate::index).collect(Collectors.toList());

        return idIndexed;
    }

    @Override
    public List<BaseUser> findAllByScroll(QueryBuilder queryBuilder) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder)
                .withScriptField(new ScriptField("isUpdated", new Script("doc.updateTime.date.isAfter(doc.createTime.date)")))
                .withScriptField(new ScriptField("full", new Script("doc.firstName.value+'_'+doc.secondName.value")))
                .withScriptField(new ScriptField("isOld", new Script("doc.age.value > 30")))
                .withSourceFilter(new FetchSourceFilterBuilder().withIncludes("*").build()).withPageable(PageRequest.of(0, 60)).build();

        ScrolledPage<BaseUser> baseUserPage = (ScrolledPage) elasticsearchTemplate.startScroll(TimeUnit.MINUTES.toMillis(3), searchQuery, BaseUser.class);
        List<BaseUser> baseUserList = new ArrayList<>((int) baseUserPage.getTotalElements());
        do {
            logger.info("当前数据条数：{}，总共数据条数：{}", baseUserPage.getNumberOfElements(), baseUserPage.getTotalElements());
            baseUserList.addAll(baseUserPage.getContent());
            baseUserPage = (ScrolledPage) elasticsearchTemplate.continueScroll(baseUserPage.getScrollId(), TimeUnit.MINUTES.toMillis(3), BaseUser.class);
        } while (!baseUserPage.getContent().isEmpty());
        return baseUserList;
    }

    @Override
    public Map<String, Integer> groupByAge(QueryBuilder queryBuilder) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder).addAggregation(AggregationBuilders.terms("groupByAge").field("age")
                .subAggregation(AggregationBuilders.min("minUpdateTime").field("updateTime"))
                .subAggregation(AggregationBuilders.max("maxUpdateTime").field("updateTime"))).build();
        AggregatedPage<BaseUser> baseUserPage = elasticsearchTemplate.queryForPage(searchQuery, BaseUser.class);
        Terms groupByAge = baseUserPage.getAggregations().get("groupByAge");
        Map<String, Integer> resultMap = new TreeMap<>(Comparator.comparingDouble(Double::valueOf));

        groupByAge.getBuckets().forEach(bucket -> {
            String key = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            resultMap.put(key, Long.valueOf(docCount).intValue());

            Min minUpdateTime = bucket.getAggregations().get("minUpdateTime");
            String min = minUpdateTime.getValueAsString();

            Max maxUpdateTime = bucket.getAggregations().get("maxUpdateTime");
            String max = maxUpdateTime.getValueAsString();

            logger.info("key={},文档数量={},minUpdateTime={},maxUpdateTime={}", key, docCount, min, max);
        });

        return resultMap;
    }

    @Override
    public Map<String, Integer> groupByUpdateTime(QueryBuilder queryBuilder) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder).addAggregation(AggregationBuilders.dateHistogram("groupByUpdateTime").field("updateTime")
                .interval(TimeUnit.DAYS.toMillis(2)).format("yyyy-MM-dd").timeZone(DateTimeZone.forOffsetHours(8)).minDocCount(10).subAggregation(AggregationBuilders.min("minAge").field("age"))
                .subAggregation(AggregationBuilders.max("maxAge").field("age"))).addAggregation(AggregationBuilders.cardinality("distinctAgeNo").field("age"))
                .addAggregation(AggregationBuilders.histogram("groupByAgeNo").field("age").interval(10).minDocCount(0).missing(20)
                        .subAggregation(AggregationBuilders.max("maxBirthDay").field("birthDay").timeZone(DateTimeZone.forOffsetHours(8))))
                .build();

        AggregatedPage<BaseUser> baseUsers = elasticsearchTemplate.queryForPage(searchQuery, BaseUser.class);
        Histogram groupByUpdateTime = baseUsers.getAggregations().get("groupByUpdateTime");

        Map<String, Integer> result = new HashMap<>(groupByUpdateTime.getBuckets().size());
        groupByUpdateTime.getBuckets().forEach(bucket -> {
            String time = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();

            Min minAge = bucket.getAggregations().get("minAge");
            double min = minAge.getValue();

            Max maxAge = bucket.getAggregations().get("maxAge");
            double max = maxAge.getValue();

            logger.info("【{}】文档数量={}，minAge={}，maxAge={}", time, docCount, min, max);
            result.put(time, Long.valueOf(docCount).intValue());
        });

        Cardinality cardinality = baseUsers.getAggregations().get("distinctAgeNo");
        long distinctAgeNo = cardinality.getValue();
        logger.info("distinct age count:{}", distinctAgeNo);

        Histogram groupByAgeNo = baseUsers.getAggregations().get("groupByAgeNo");
        groupByAgeNo.getBuckets().forEach(bucket -> {
            String age = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();

            Max maxBirthDay = bucket.getAggregations().get("maxBirthDay");
            String max = maxBirthDay.getValueAsString();
            logger.info("age={},docCount={},maxBirthDay={}", age, docCount, max);
        });

        return result;
    }
}