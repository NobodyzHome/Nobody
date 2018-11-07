package com.spring.es.repository;

import com.alibaba.fastjson.JSON;
import com.spring.data.domain.BaseLine;
import com.spring.data.domain.generator.DomainGenerator;
import com.spring.service.repository.LineRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-es.xml")
public class LineRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(LineRepositoryTest.class);

    @Autowired
    private LineRepository lineRepository;

    @Test
    public void test1() {
        Iterable<BaseLine> allBaseLine = lineRepository.findAll();
        Page<BaseLine> baseLinePage = (Page) allBaseLine;
        int numberOfElements = baseLinePage.getNumberOfElements();
        long totalElements = baseLinePage.getTotalElements();
        String collect = baseLinePage.getContent().stream().map(BaseLine::getLineName).collect(Collectors.joining(","));
        logger.info("当前查询的文档数量：{}，总共的文档数据：{}，数据内容：{}", numberOfElements, totalElements, collect);

        Pageable pageable = PageRequest.of(0, 50);
        do {
            Page<BaseLine> page = lineRepository.findAll(pageable);
            int numberOfElements1 = page.getNumberOfElements();
            long totalElements1 = page.getTotalElements();
            String lineNameCollect = page.getContent().stream().map(BaseLine::getLineName).collect(Collectors.joining(","));
            logger.info("{} / {}，获取到的数据内容:{}", numberOfElements1, totalElements1, lineNameCollect);

            if (page.hasNext()) {
                pageable = page.nextPageable();
            } else {
                break;
            }
        } while (true);
    }

    @Test
    public void test2() {
        BaseLine line = new BaseLine();
        line.setId("8791");
        line.setLineNo(8791);
        line.setLineName("8791路");
        BaseLine baseLine = lineRepository.index(line);
        logger.info("保存的数据内容：{}，文档ID={}", JSON.toJSONString(baseLine), baseLine.getId());

        line.setLineName("第8791路");
        line.setLineCode("No-8791");
        line.setCreateTime(new Date());
        BaseLine baseLineUpdated = lineRepository.save(line);
        logger.info("更新后的数据内容：{}", JSON.toJSONString(baseLineUpdated));
    }

    @Test
    public void test3() {
        String id = "nc5_3WYBSspZuDYFGW2l";
        lineRepository.deleteById(id);
        boolean existsById = lineRepository.existsById(id);
        assert !existsById;
    }

    @Test
    public void test4() {
        Optional<BaseLine> byId = lineRepository.findById("8791");
        byId.ifPresent(id -> logger.info(byId.toString()));
    }

    @Test
    public void test5() {
        BaseLine line = new BaseLine();
        line.setLineNo(9911);
        line.setLineCode("No-9911");
        line.setLineName("第9911路");

        BaseLine lineIndexed = lineRepository.save(line);
        logger.info("索引的文档的id:{}", lineIndexed.getId());

        Iterable<BaseLine> lineNoIterable = lineRepository.search(QueryBuilders.matchQuery("lineNo", line.getLineNo()));
        for (BaseLine baseLine : lineNoIterable) {
            logger.info("获取到的数据：{}", JSON.toJSONString(baseLine));
            lineRepository.delete(baseLine);
        }
    }

    @Test
    public void test6() {
        List<BaseLine> baseLineList = lineRepository.findByLineNo(2359);
        baseLineList.stream().map(JSON::toJSONString).forEach(logger::info);

        Pageable pageable = PageRequest.of(1, 100);
        Page<BaseLine> baseLinePage = lineRepository.findByLineName("第2359路", pageable);
        logger.info("查询到的数据条数={}，总数据条数={}", baseLinePage.getNumberOfElements(), baseLinePage.getTotalElements());
    }

    @Test
    public void test7() {
        boolean isSuccess = lineRepository.createIndex(BaseLine.class);
        logger.info("创建索引：{}", isSuccess ? "成功" : "失败");
    }

    @Test
    public void test8() {
        List<BaseLine> baseLineList = lineRepository.scrollAll(QueryBuilders.matchAllQuery());
        logger.info("共获取{}条数据！", baseLineList.size());
    }

    @Test
    public void test9() {
        List<BaseLine> baseLineList = DomainGenerator.generateLines(false, 1000);
        lineRepository.saveAll(baseLineList);

        // 测试如果修改了@ReadonlyProperties的属性，但在保存到ES里时，不会把这些属性保存到文档中
        BaseLine baseLine1 = baseLineList.get(0);
        baseLine1.setLineName("wa ha ha wu ga ga!");
        baseLine1.setLineCode("yo ho ho!!!");
        baseLine1.setUpdated(false);
        baseLine1.setScore(15.22f);
        BaseLine indexedBaseLine = lineRepository.index(baseLine1);
        logger.info("修改的文档的id:{}", indexedBaseLine.getId());
        BaseLine baseLineSearched = lineRepository.findById(indexedBaseLine.getId()).get();
        logger.info("查询到的文档：{}", JSON.toJSONString(baseLineSearched));

        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("lineNo").gte(2000).lt(3000)).must(QueryBuilders.matchQuery("lineName", "路")))
                .withScriptField(new ScriptField("isUpdated", new Script("doc.updateTime.date.isAfter(doc.createTime.date)")))
                .withScriptField(new ScriptField("multiLineNo", new Script("doc.lineNo.value * 2")))
                .withSourceFilter(new FetchSourceFilterBuilder().withIncludes("*").build()).build();
        Iterable<BaseLine> baseLinesSearched = lineRepository.search(searchQuery);
        baseLinesSearched.forEach(baseLine -> logger.info("获取到的数据：{}", JSON.toJSONString(baseLine)));
    }

    @Test
    public void test10() {
        List<BaseLine> baseLineList = lineRepository.searchAfterAll(QueryBuilders.matchAllQuery());
        logger.info("共获取{}个文档！", baseLineList.size());
    }

    @Test
    public void test11() {
        Map<String, Integer> map = lineRepository.histogramByLineNo(QueryBuilders.matchAllQuery());
        logger.info("获取到数据：{}", map);
    }

    @Test
    public void test12() {
        Map<Date, Integer> map = lineRepository.dateHistogramByUpdateTime(QueryBuilders.matchAllQuery());
        logger.info("获取到数据：{}", map);
    }
}
