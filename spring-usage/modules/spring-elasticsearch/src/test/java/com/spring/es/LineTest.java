package com.spring.es;

import com.alibaba.fastjson.JSON;
import com.spring.data.domain.BaseLine;
import com.spring.data.domain.generator.DomainGenerator;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ScrolledPage;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:spring-es.xml")
public class LineTest {

    private static final Logger logger = LoggerFactory.getLogger(LineTest.class);

    @Autowired
    private ElasticsearchTemplate template;

    @Test
    public void testCreateIndex() {
        if (template.indexExists(BaseLine.class)) {
            boolean deleteIndex = template.deleteIndex(BaseLine.class);
            logger.info("由于索引已存在，因此先删除该索引！索引删除【{}】", deleteIndex ? "成功" : "失败");
        }
        boolean indexCreateSuccess = template.createIndex(BaseLine.class);
        boolean mappingCreateSuccess = template.putMapping(BaseLine.class);

        logger.info("创建索引【{}】", indexCreateSuccess ? "成功" : "失败");
        logger.info("创建映射【{}】", mappingCreateSuccess ? "成功" : "失败");

        Map setting = template.getSetting(BaseLine.class);
        Map mapping = template.getMapping(BaseLine.class);
        logger.info("当前索引的setting为：{}", setting);
        logger.info("当前索引的mapping为：{}", mapping);
    }

    @Test
    public void testIndexData() {
        BaseLine baseLine = DomainGenerator.generateLine(8888, false);
        String idSaved = template.index(new IndexQueryBuilder().withObject(baseLine).build());
        logger.info("索引文档的ID为：{}", idSaved);

        BaseLine baseLine1 = template.queryForObject(new CriteriaQuery(Criteria.where("lineNo").is(baseLine.getLineNo())), BaseLine.class);
        logger.info("获取到的文档为：{}", JSON.toJSONString(baseLine1));

        BaseLine newBaseLine = new BaseLine();
        newBaseLine.setId(baseLine1.getId());
        newBaseLine.setLineName("wa ho ho");
        String index = template.index(new IndexQueryBuilder().withObject(newBaseLine).build());
        logger.info("索引文档的ID为：{}", index);

        String delete = template.delete(BaseLine.class, baseLine1.getId());
        logger.info("删除文档的ID为：{}", delete);
    }

    @Test
    public void testIndexDatas() {
        List<BaseLine> baseLineList = DomainGenerator.generateLines(true, 1000);
        baseLineList.forEach(baseLine -> logger.info(template.index(new IndexQueryBuilder().withObject(baseLine).build())));
    }

    @Test
    public void testQuery() {
        Pageable pageable = PageRequest.of(0, 50);
        do {
            SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).withPageable(pageable)
                    .withScriptField(new ScriptField("isUpdated", new Script("doc.updateTime.date.isAfter(doc.createTime.date)")))
                    .withScriptField(new ScriptField("multiLineNo", new Script("2 * doc.lineNo.value")))
                    .withSourceFilter(new FetchSourceFilter(new String[]{"*"}, null)).build();
            AggregatedPage<BaseLine> baseLinePage = template.queryForPage(searchQuery, BaseLine.class);
            List<BaseLine> baseLineList = baseLinePage.getContent();
            logger.info("{}/{}，数据内容：{}。【{}】", baseLinePage.getPageable().getPageNumber() + 1, baseLinePage.getTotalPages(), JSON.toJSONString(baseLineList), pageable);

            if (baseLinePage.hasNext()) {
                pageable = baseLinePage.nextPageable();
            } else {
                break;
            }

        } while (true);
    }

    @Test
    public void testAggregation() {
        SearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("lineName", "路"))
                .withScriptField(new ScriptField("isUpdated", new Script("doc.updateTime.date.isAfter(doc.createTime.date)")))
                .withScriptField(new ScriptField("multiLineNo", new Script("2 * doc.lineNo.value")))
                .withSourceFilter(new FetchSourceFilter(new String[]{"*"}, null))
                .addAggregation(AggregationBuilders.histogram("groupByLineNo").field("lineNo").interval(50).minDocCount(10).missing(1111)
                        .subAggregation(AggregationBuilders.max("maxUpdateTime").field("updateTime")).subAggregation(AggregationBuilders.sum("sumLineNo").field("lineNo")))
                .addAggregation(AggregationBuilders.dateHistogram("groupByUpdateTime").field("updateTime").interval(TimeUnit.HOURS.toMillis(2)).format("yyyy-MM-dd HH").minDocCount(90)
                        .timeZone(DateTimeZone.forOffsetHours(8)).subAggregation(AggregationBuilders.sum("sumLineNo").field("lineNo")))
                .build();
        AggregatedPage<BaseLine> baseLinePage = template.queryForPage(query, BaseLine.class);

        Histogram groupByLineNo = baseLinePage.getAggregations().get("groupByLineNo");
        groupByLineNo.getBuckets().forEach(bucket -> {
            String lineNoStr = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();

            Max max = bucket.getAggregations().get("maxUpdateTime");
            String maxUpdateTime = max.getValueAsString();

            Sum sum = bucket.getAggregations().get("sumLineNo");
            double sumLineNo = sum.getValue();
            logger.info("【{}】文档数量：{}，maxUpdateTime={},sumLineNo={}", lineNoStr, docCount, maxUpdateTime, sumLineNo);
        });

        Histogram groupByUpdateTime = baseLinePage.getAggregations().get("groupByUpdateTime");
        groupByUpdateTime.getBuckets().forEach(bucket -> {
            String dateStr = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            Sum sum = bucket.getAggregations().get("sumLineNo");
            double sumLineNo = sum.getValue();
            logger.info("【{}】文档数量：{}，sumLineNo={}", dateStr, docCount, sumLineNo);
        });
    }

    @Test
    public void testScroll() {
        Pageable pageable = PageRequest.of(0, 100);
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).withPageable(pageable)
                .withScriptField(new ScriptField("multiLineNo", new Script("2 * doc.lineNo.value")))
                .withScriptField(new ScriptField("isUpdated", new Script("doc.updateTime.date.isAfter(doc.createTime.date)")))
                .withSourceFilter(new FetchSourceFilter(new String[]{"*"}, null)).build();

        ScrolledPage<BaseLine> baseLinePage = (ScrolledPage) template.startScroll(TimeUnit.MINUTES.toMillis(2), searchQuery, BaseLine.class);
        do {
            List<BaseLine> baseLineList = baseLinePage.getContent();
            logger.info("scroll_id={}，获取到的数据内容：{}", baseLinePage.getScrollId(), JSON.toJSONString(baseLineList));

            baseLinePage = (ScrolledPage) template.continueScroll(baseLinePage.getScrollId(), TimeUnit.MINUTES.toMillis(2), BaseLine.class);
        } while (!baseLinePage.getContent().isEmpty());
        template.clearScroll(baseLinePage.getScrollId());
    }

    @Test
    public void testSearchAfter() {
        Object[] sortValues = null;
        Client client = template.getClient();
        DefaultResultMapper resultMapper = new DefaultResultMapper();

        int position = 1;
        do {
            SearchRequestBuilder searchRequestBuilder = client.prepareSearch("index_line").setQuery(QueryBuilders.matchAllQuery())
                    .addScriptField("multiLineNo", new Script("2 * doc.lineNo.value"))
                    .addScriptField("isUpdated", new Script("doc.updateTime.date.isAfter(doc.createTime.date)"))
                    .setSize(100).setFetchSource("*", null)
                    .addAggregation(AggregationBuilders.dateHistogram("groupByUpdateTime").field("updateTime").format("yyyy-MM-dd HH").interval(TimeUnit.HOURS.toMillis(2))
                            .minDocCount(90).timeZone(DateTimeZone.forOffsetHours(8))
                            .subAggregation(AggregationBuilders.sum("sumLineNo").field("lineNo"))
                            .subAggregation(AggregationBuilders.max("maxUpdateTime").field("updateTime")))
                    .addSort("lineNo", SortOrder.DESC);
            Optional.ofNullable(sortValues).ifPresent(searchRequestBuilder::searchAfter);

            SearchResponse searchResponse = searchRequestBuilder.get();
            SearchHits hits = searchResponse.getHits();

            if (ArrayUtils.isEmpty(hits.getHits())) {
                Histogram groupByUpdateTime = searchResponse.getAggregations().get("groupByUpdateTime");
                groupByUpdateTime.getBuckets().forEach(bucket -> {
                    String timeStr = bucket.getKeyAsString();
                    long docCount = bucket.getDocCount();

                    Sum sum = bucket.getAggregations().get("sumLineNo");
                    String sumLineNo = sum.getValueAsString();

                    Max max = bucket.getAggregations().get("maxUpdateTime");
                    String dateStr = max.getValueAsString();

                    logger.info("时间：{}，文档数量：{},sumLineNo={}，maxUpdateTime={}", timeStr, docCount, sumLineNo, dateStr);
                });
                break;
            }

            for (SearchHit hit : hits) {
                BaseLine baseLine = resultMapper.mapEntity(hit.getSourceAsString(), BaseLine.class);
                logger.info("【{}/{}】获取到的数据：{}", position++, hits.getTotalHits(), JSON.toJSONString(baseLine));

                if (hit.equals(hits.getAt(hits.getHits().length - 1))) {
                    sortValues = hit.getSortValues();
                }
            }
        } while (true);
    }

    @Test
    public void testUpdateDoc() {
        List<BaseLine> baseLineList = template.queryForList(new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).build(), BaseLine.class);
        BaseLine baseLine = baseLineList.get(0);
        logger.info("要修改的文档的ID={}", baseLine.getId());

        baseLine.setLineName("wa ga ga!");
        BeanMap doc = new BeanMap(baseLine);

        UpdateRequest updateRequest = template.getClient().prepareUpdate().setDoc(doc).request();
        UpdateQuery updateQuery = new UpdateQueryBuilder().withClass(BaseLine.class).withId(baseLine.getId()).withUpdateRequest(updateRequest).build();
        UpdateResponse updateResponse = template.update(updateQuery);
        logger.info("index={},type={},id={},version={},分片修改情况：{}", updateResponse.getIndex(), updateResponse.getType(), updateResponse.getId(), updateResponse.getVersion(), updateResponse.getShardInfo());

        GetQuery query = new GetQuery();
        query.setId(baseLine.getId());
        BaseLine baseLineSearched = template.queryForObject(query, BaseLine.class);
        logger.info("查询修改后的文档数据：{}", JSON.toJSONString(baseLineSearched));
    }
}