package com.spring.es;

import com.google.common.collect.Lists;
import com.spring.data.domain.BaseDept;
import com.spring.data.domain.generator.DomainGenerator;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCountAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ScrolledPage;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-es.xml")
public class DeptTest {

    private Logger logger = LoggerFactory.getLogger(DeptTest.class);

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void testCreateIndex() {
        try {
            logger.info("开始执行索引创建！");
            if (elasticsearchTemplate.indexExists(BaseDept.class)) {
                boolean deleteIndexSuccess = elasticsearchTemplate.deleteIndex(BaseDept.class);
                logger.info("由于当前索引已存在，因此删除该索引！删除索引【{}】！", deleteIndexSuccess ? "成功" : "失败");
            }
            // 创建索引后，索引的settings属性有值，mappings属性是没有值的，需要再调用putMapping方法设置映射。使用createIndex方法时，BaseDept类上必须增加@Document注解
            boolean createIndexSuccess = elasticsearchTemplate.createIndex(BaseDept.class);
            // 创建映射时，BaseDept类的属性必须加上@Field注解，同时哪个字段是用来给_id属性赋值的，那么必须在那个字段上加上@Id注解
            boolean createMappingSuccess = elasticsearchTemplate.putMapping(BaseDept.class);

            logger.info("创建索引【{}】！", createIndexSuccess ? "成功" : "失败");
            logger.info("创建映射【{}】！", createMappingSuccess ? "成功" : "失败");
        } catch (Exception e) {
            logger.error("创建索引失败", e);
        }
    }

    @Test
    public void test2() {
        Map setting = elasticsearchTemplate.getSetting(BaseDept.class);
        System.out.println(setting);
    }

    @Test
    public void test3() {
        int from = 675, to = 5000;
        List<Integer> deptNos = new ArrayList<>(100);
        for (int num = from; num <= to; num++) {
            deptNos.add(num);
        }

        List<BaseDept> baseDepts = DomainGenerator.generateDepts(deptNos.toArray(new Integer[0]));
        baseDepts.stream().parallel().map(dept -> new IndexQueryBuilder().withObject(dept).build()).forEach(elasticsearchTemplate::index);
    }

    @Test
    public void test4() {
        // spring-data-es提供了多种查询对象：CriteriaQuery、SearchQuery、StringQuery、GetQuery，可以使用这些Query来传递给EsTemplate，进行查询
        // 1.使用spring-data-es提供的CriteriaQuery进行查询
        BaseDept dept = elasticsearchTemplate.queryForObject(new CriteriaQuery(Criteria.where("deptNo").is(22)), BaseDept.class);
        System.out.println(dept);

        // 2.使用spring-data-es提供的SearchQuery进行查询，这时可以使用es的jar包中原有的QueryBuilder
        List<BaseDept> baseDepts = elasticsearchTemplate.queryForList(new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("deptEnName", "DEPT-42"))
                .withFields("deptNo", "deptName", "deptEnName").build(), BaseDept.class);
        System.out.println(baseDepts);

        // 3.使用spring-data-es提供的StringQuery进行查询，此时可以直接写es的查询语句。注意：不用写{"query":}
        List<BaseDept> baseDepts1 = elasticsearchTemplate.queryForList(new StringQuery("{\"range\":{\"level\":{\"gte\":10,\"lt\":15}}}"), BaseDept.class);
        System.out.println(baseDepts1);

        // 4.使用spring-data-es提供的GetQuery进行查询，相当于使用指定ID来进行GET查询
        GetQuery getQuery = new GetQuery();
        getQuery.setId(dept.getDeptNo().toString());
        BaseDept baseDept = elasticsearchTemplate.queryForObject(getQuery, BaseDept.class);
        System.out.println(baseDept);
    }

    @Test
    public void test5() {
        BaseDept baseDept = elasticsearchTemplate.queryForObject(new StringQuery("{\"match\":{\"deptNo\":9999}}"), BaseDept.class);
        if (Objects.nonNull(baseDept)) {
            BaseDept dept = new BaseDept();
            // 由于BaseDept类中deptNo属性给出了@Id注解，因此在保存该对象时，会把deptNo的属性值作为文档的_id保存起来，相当于PUT /index/type/id方式的存储
            dept.setDeptNo(9999);
            dept.setDeptName("测试123456");
            dept.setModifyDate(new Date());

            // 使用index方法既可以进行数据的添加，也可以进行PUT的方式的更新，即整个文档的替换
            // 如果给出了ID（withId），那么就是将Object整个替换到指定id的文档中。如果没有给出ID，那就是新增一个文档
            String id = elasticsearchTemplate.index(new IndexQueryBuilder().withId(baseDept.getDeptNo().toString()).withObject(dept).build());
            System.out.println(id);

            GetQuery getQuery = new GetQuery();
            getQuery.setId(id);
            BaseDept baseDept1 = elasticsearchTemplate.queryForObject(getQuery, BaseDept.class);
            System.out.println(baseDept1);

            String idDeleted = elasticsearchTemplate.delete(BaseDept.class, id);
            System.out.println(idDeleted);
        }
    }

    @Test
    public void testPage() {
        // 在SearchQuery、StringQuery、CriteriaQuery中都可以设置Pageable，进行分页设置
        // 1.使用SearchQuery是通过new NativeSearchQueryBuilder().withPageable().build()方法来设置分页
        // 2.使用StringQuery是通过new StringQuery(str,Pageable)来设置分页
        // 3.使用CriteriaQuery是通过new CriteriaQuery(criteria,Pageable)来设置分页
        long count = elasticsearchTemplate.count(new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("deptEnName", "41-DEPTS")).build(), BaseDept.class);
        System.out.println(count);
        Pageable pageable = PageRequest.of(0, 30);
        do {
            StringQuery stringQuery = new StringQuery("{\"match\":{\"deptEnName\":\"41-DEPTS\"}}");
            stringQuery.setPageable(pageable);

            Page<BaseDept> baseDeptPage = elasticsearchTemplate.queryForPage(stringQuery, BaseDept.class);
            baseDeptPage.stream().forEach(baseDept -> System.out.println(baseDeptPage + ":" + baseDept.getDeptName()));
            if (baseDeptPage.hasNext()) {
                pageable = baseDeptPage.nextPageable();
            } else {
                break;
            }
        } while (true);

        Pageable pageable1 = PageRequest.of(0, 1000);
        List<String> ids = elasticsearchTemplate.queryForIds(new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).withPageable(pageable1).build());
        System.out.println(ids);
    }

    @Test
    public void testScroll() {
        // Pageable是分页的查询条件，可以在其中设置要查询第几页(page，从0开始)以及每页要获取的数据条数(size)
        Pageable pageable = PageRequest.of(0, 1000);
        // Page是分页的查询结果，子接口ScrolledPage可以获取当前页的scrollId，子接口AggregatedPage可以获取当前页的聚合情况
        Page<BaseDept> baseDeptPage = elasticsearchTemplate.startScroll(TimeUnit.MINUTES.toMillis(2), new CriteriaQuery(Criteria.where("deptEnName").is("depted"), pageable), BaseDept.class);
        do {
            System.out.println(baseDeptPage.stream().map(BaseDept::getDeptName).collect(Collectors.joining(",")));
            System.out.println(baseDeptPage.getNumberOfElements());
            baseDeptPage = elasticsearchTemplate.continueScroll(((ScrolledPage) baseDeptPage).getScrollId(), TimeUnit.MINUTES.toMillis(2), BaseDept.class);
        } while (baseDeptPage.getNumberOfElements() > 0);

        elasticsearchTemplate.clearScroll(((ScrolledPage) baseDeptPage).getScrollId());
    }

    @Test
    public void testAggregation() {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("deptEnName"
                , "depts")).withPageable(PageRequest.of(2, 500));
        nativeSearchQueryBuilder.addAggregation(new TermsAggregationBuilder("groupByDeptName", ValueType.STRING).field("deptName1"))
                .addAggregation(new TermsAggregationBuilder("groupByRemark", ValueType.STRING).field("remark"));

        AggregatedPage<BaseDept> baseDepts = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), BaseDept.class);
        Terms groupByDeptName = baseDepts.getAggregations().get("groupByDeptName");
        Terms groupByRemark = baseDepts.getAggregations().get("groupByRemark");

        groupByDeptName.getBuckets().forEach(bucket -> System.out.println(bucket.getKeyAsString() + ":" + bucket.getDocCount()));
        groupByRemark.getBuckets().forEach(bucket -> System.out.println(bucket.getKeyAsString() + ":" + bucket.getDocCount()));
    }

    @Test
    public void testUpdateDoc() {
        GetQuery getQuery = new GetQuery();
        getQuery.setId("50");
        BaseDept baseDept = elasticsearchTemplate.queryForObject(getQuery, BaseDept.class);

        Map doc = new HashMap();
        doc.put("deptCode", "50 HA HA");

        UpdateQueryBuilder updateQueryBuilder = new UpdateQueryBuilder().withId(baseDept.getDeptNo().toString()).withClass(BaseDept.class)
                .withUpdateRequest(elasticsearchTemplate.getClient().prepareUpdate().setDoc(doc).request());
        UpdateResponse updateResponse = elasticsearchTemplate.update(updateQueryBuilder.build());
        System.out.println(updateResponse.getId());
    }

    @Test
    public void testAggs() {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery())
                .addAggregation(new HistogramAggregationBuilder("group")
                        //注意：interval的单位是毫秒，如果需要按1小时汇总，必须要传入1小时对应的毫秒数
                        .interval(TimeUnit.HOURS.toMillis(1)).field("createTime")
                        .format("yyyy-MM-dd HH").timeZone(DateTimeZone.forOffsetHours(8)));
        AggregatedPage<BaseDept> deptAggr = elasticsearchTemplate.queryForPage(queryBuilder.build(), BaseDept.class);

        Histogram histogram = deptAggr.getAggregations().get("group");
        histogram.getBuckets().stream().forEach(bucket -> logger.info("汇总的key是：{}，这个key下的文档数量是：{}", bucket.getKeyAsString(), bucket.getDocCount()));
    }

    @Test
    public void testUpdateDoc1() {
        GetQuery query = new GetQuery();
        String id = "AAABBB";
        query.setId(id);
        BaseDept baseDept = elasticsearchTemplate.queryForObject(query, BaseDept.class);

        if (Objects.isNull(baseDept)) {
            logger.warn("未获取到该ID对应的数据。id={}", id);

            BaseDept dept = new BaseDept();
            dept.setModifyDate(new Date());
            dept.setCreateTime(new Date());
            String idSaved = elasticsearchTemplate.index(new IndexQueryBuilder().withId(id).withObject(dept).build());
            logger.debug("由于没有获取到ID为{}的数据，因此插入数据，插入数据的ID为{}", id, idSaved);
        }

        Map<String, Object> doc = new HashMap<>();
        doc.put("modifyDate", DateUtils.addHours(new Date(), -3));

        UpdateRequest updateRequest = elasticsearchTemplate.getClient().prepareUpdate().setDoc(doc).request();
        UpdateResponse updateResponse = elasticsearchTemplate.update(new UpdateQueryBuilder().withClass(BaseDept.class).withId(id).withDoUpsert(false).withUpdateRequest(updateRequest).build());
        String idUpdated = updateResponse.getId();
        logger.info("更新的ID为{}，分片更新情况：{}", idUpdated, updateResponse.getShardInfo());

        BaseDept baseDept1 = elasticsearchTemplate.queryForObject(query, BaseDept.class);
        logger.info(baseDept1.getModifyDate().toString());
        logger.info(baseDept1.getCreateTime().toString());
    }

    @Test
    public void testDateHistogram() {
        DateHistogramAggregationBuilder histogramAggregationBuilder = new DateHistogramAggregationBuilder("groupByCreateTime");
        histogramAggregationBuilder.interval(TimeUnit.HOURS.toMillis(1)).field("createTime").timeZone(DateTimeZone.forOffsetHours(8)).format("yyyy-MM-dd HH");

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).addAggregation(histogramAggregationBuilder).build();
        AggregatedPage<BaseDept> baseDeptAggregatedPage = elasticsearchTemplate.queryForPage(searchQuery, BaseDept.class);
        List<BaseDept> content = baseDeptAggregatedPage.getContent();
        logger.info("获取到{}条数据，共有{}条数据！", content.size(), baseDeptAggregatedPage.getTotalElements());
        Histogram groupByCreateTime = baseDeptAggregatedPage.getAggregations().get("groupByCreateTime");
        groupByCreateTime.getBuckets().forEach(bucket -> logger.info("{} : {}", bucket.getKeyAsString(), bucket.getDocCount()));
    }

    @Test
    public void indexDatas() {
        if (elasticsearchTemplate.indexExists(BaseDept.class)) {
            boolean deleteIndex = elasticsearchTemplate.deleteIndex(BaseDept.class);
            logger.info("删除索引【{}】", deleteIndex ? "成功" : "失败");
        }
        boolean indexCreate = elasticsearchTemplate.createIndex(BaseDept.class);
        boolean mappingCreate = elasticsearchTemplate.putMapping(BaseDept.class);
        logger.info("创建索引【{}】", indexCreate ? "成功" : "失败");
        logger.info("创建映射【{}】", mappingCreate ? "成功" : "失败");


        List<BaseDept> baseDepts = DomainGenerator.generateDepts(1000);
        baseDepts.forEach(baseDept -> elasticsearchTemplate.index(new IndexQueryBuilder().withObject(baseDept).build()));
    }

    @Test
    public void testPageable() {
        Pageable pageable = PageRequest.of(0, 20);
        do {
            StringQuery query = new StringQuery("{\"match_all\":{}}", pageable);
            Page<BaseDept> baseDeptPage = elasticsearchTemplate.queryForPage(query, BaseDept.class);
            StringJoiner joiner = new StringJoiner(",");
            baseDeptPage.getContent().stream().map(BaseDept::getDeptName).forEach(joiner::add);
            logger.info("查询出的数据：{}，分页情况：{}", joiner.toString(), baseDeptPage.getPageable());
            if (baseDeptPage.hasNext()) {
                pageable = baseDeptPage.getPageable().next();
            } else {
                break;
            }
        } while (true);
    }

    @Test
    public void testScroll1() {
        Pageable pageable = PageRequest.of(0, 200);
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).withPageable(pageable).build();
        // 在使用scroll进行查询时，返回的Page对象不是分页的baseDeptPage.getPageable().isPaged()==false，这样就不能使用baseDeptPage.hasNext()来判断是否后面还有分页数据
        ScrolledPage<BaseDept> baseDeptPage = (ScrolledPage) elasticsearchTemplate.startScroll(TimeUnit.MINUTES.toMillis(2), searchQuery, BaseDept.class);

        do {
            StringJoiner joiner = new StringJoiner(",");
            baseDeptPage.getContent().stream().map(BaseDept::getDeptName).forEach(joiner::add);
            logger.info(joiner.toString());

            baseDeptPage = (ScrolledPage) elasticsearchTemplate.continueScroll(baseDeptPage.getScrollId(), TimeUnit.MINUTES.toMillis(2), BaseDept.class);
        } while (!baseDeptPage.getContent().isEmpty());
        elasticsearchTemplate.clearScroll(baseDeptPage.getScrollId());
    }

    @Test
    public void testAggregation1() {
        // ES一般使用Builder结尾的内容作为查询条件，例如QueryBuilder，AggregationBuilder，而生成他们通常使用QueryBuilders，AggregationBuilders工具类
        // ES在索引、更新、删除一个文档时，使用IndexRequestBuilder、UpdateRequestBuilder、DeleteRequestBuilder，生成这些对象时，通常使用ES的客户端的prepareIndex、prepareUpdate、prepareDelete方法
        HistogramAggregationBuilder histogramAggregationBuilder = AggregationBuilders.histogram("groupByCreateTime").format("yyyy-MM-dd HH").interval(TimeUnit.HOURS.toMillis(1)).field("createTime").timeZone(DateTimeZone.forOffsetHours(8));
        histogramAggregationBuilder.subAggregation(AggregationBuilders.sum("sumLevel").field("level")).subAggregation(AggregationBuilders.cardinality("distinctLevel").field("level"));

        // AggregationBuilder.size()属性可以设置聚合里最多的桶的数量，如果不设置则为10
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("groupByLevel").field("level").size(100).subAggregation(AggregationBuilders.max("maxCreateTime").field("createTime").timeZone(DateTimeZone.forOffsetHours(8)))
                .subAggregation(AggregationBuilders.min("minCreateTime").field("createTime").timeZone(DateTimeZone.forOffsetHours(8)));
        ValueCountAggregationBuilder countAggregationBuilder = AggregationBuilders.count("countAllDept").field("deptNo");

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).addAggregation(histogramAggregationBuilder)
                .addAggregation(termsAggregationBuilder).addAggregation(countAggregationBuilder).build();
        AggregatedPage<BaseDept> baseDeptAggregatedPage = elasticsearchTemplate.queryForPage(searchQuery, BaseDept.class);

        ValueCount aggregation1 = baseDeptAggregatedPage.getAggregations().get("countAllDept");
        long value = aggregation1.getValue();
        logger.info("共有{}条公司数据", value);

        Histogram aggregation2 = baseDeptAggregatedPage.getAggregations().get("groupByCreateTime");
        aggregation2.getBuckets().forEach((Histogram.Bucket bucket) -> {
            String time = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();

            Sum sum = bucket.getAggregations().get("sumLevel");
            double sumLevel = sum.getValue();

            Cardinality distinctLevel = bucket.getAggregations().get("distinctLevel");
            long distinctLevelValue = distinctLevel.getValue();

            logger.info("当前汇总的时间为：{}，共有文档数量：{}，level总和：{}，level的不重复数量：{}", time, docCount, sumLevel, distinctLevelValue);
        });

        Terms aggregation3 = baseDeptAggregatedPage.getAggregations().get("groupByLevel");
        aggregation3.getBuckets().forEach((Terms.Bucket bucket) -> {
            String level = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();

            Max max = bucket.getAggregations().get("maxCreateTime");
            Min min = bucket.getAggregations().get("minCreateTime");
            String maxCreateTime = max.getValueAsString();
            String minCreateTime = min.getValueAsString();

            logger.info("当前汇总的level是：{}，共有{}个文档，最大createTime：{}，最小createTime：{}", level, docCount, maxCreateTime, minCreateTime);
        });
    }

    @Test
    public void testScriptedField() {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("deptName", "1642公司"))
                .withScriptField(new ScriptField("prefixRemark", new Script("'pre:'+doc.remark.value"))).build();

        List<BaseDept> baseDepts = elasticsearchTemplate.queryForList(searchQuery, BaseDept.class);
        String collect = baseDepts.stream().map(BaseDept::getPrefixRemark).collect(Collectors.joining(","));
        logger.info(collect);
    }

    @Test
    public void testSearchAfter() {
        Object[] sortValues = null;
        do {
            SearchRequestBuilder searchRequestBuilder = elasticsearchTemplate.getClient().prepareSearch("index_dept").setQuery(QueryBuilders.matchAllQuery())
                    .addSort(SortBuilders.fieldSort("level").order(SortOrder.DESC)).addSort(SortBuilders.fieldSort("createTime").order(SortOrder.ASC)).setSize(80);

            if (ArrayUtils.isNotEmpty(sortValues)) {
                searchRequestBuilder.searchAfter(sortValues);
            }

            List<BaseDept> baseDeptList = Lists.newArrayList();
            SearchResponse searchResponse = searchRequestBuilder.get();
            if (ArrayUtils.isEmpty(searchResponse.getHits().getHits())) {
                break;
            } else {
                for (SearchHit hit : searchResponse.getHits()) {
                    try {
                        DefaultResultMapper resultMapper = new DefaultResultMapper();
                        BaseDept baseDept = resultMapper.mapEntity(hit.getSourceAsString(), BaseDept.class);
                        baseDeptList.add(baseDept);
                    } catch (Exception e) {
                        logger.error(String.format("创建BaseDept对象的属性报错！source值=%s", hit.getSourceAsString()), e);
                    }

                    if (hit.equals(searchResponse.getHits().getAt(searchResponse.getHits().getHits().length - 1))) {
                        sortValues = hit.getSortValues();
                    }
                }

                String deptNameCollect = baseDeptList.stream().map(BaseDept::getDeptName).collect(Collectors.joining(","));
                logger.info("获取到{}条数据：{}", baseDeptList.size(), deptNameCollect);
            }

        } while (true);
    }
}
