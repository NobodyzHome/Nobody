package com.spring.es.repository;

import com.alibaba.fastjson.JSON;
import com.spring.data.domain.BaseTerminal;
import com.spring.data.domain.generator.DomainGenerator;
import com.spring.service.repository.TerminalRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-es.xml")
public class TerminalTest {

    private static Logger logger = LoggerFactory.getLogger(TerminalTest.class);

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private TerminalRepository terminalRepository;

    @Test
    public void testCreateIndex() {
        if (template.indexExists(BaseTerminal.class)) {
            boolean indexDelete = template.deleteIndex(BaseTerminal.class);
            logger.debug("由于索引已存在，因此需要先删除索引！删除索引【{}】", indexDelete ? "成功" : "失败");
        }
        boolean indexCreated = template.createIndex(BaseTerminal.class);
        boolean mappingCreated = template.putMapping(BaseTerminal.class);
        logger.info("创建索引【{}】", indexCreated && mappingCreated ? "成功" : "失败");
    }

    @Test
    public void testIndexData() {
        BaseTerminal terminal = DomainGenerator.generateTerminal(300);
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setObject(terminal);

        String id = template.index(indexQuery);
        logger.info("索引的文档的ID：{}", id);

        BaseTerminal terminalSearched = template.queryForObject(new CriteriaQuery(Criteria.where("lineNo").is(terminal.getLineNo())), BaseTerminal.class);
        logger.info("查询到的文档：{}", JSON.toJSONString(terminalSearched));
    }

    @Test
    public void testSearchData() {
        List<BaseTerminal> byCustom = terminalRepository.findByCustom("车", 400);
        logger.info("获取到的数据为：{}", JSON.toJSONString(byCustom));

        Iterable<BaseTerminal> all = terminalRepository.findAll();
        BaseTerminal terminal = all.iterator().next();
        logger.info("获取到的数据为：{}", JSON.toJSONString(terminal));

        Optional<BaseTerminal> findById = terminalRepository.findById(terminal.getTerminalNo());
        BaseTerminal terminal1 = findById.get();

        Stream<BaseTerminal> findByLineNo = terminalRepository.findByLineNo(terminal1.getLineNo());
        findByLineNo.forEach(baseTerminal -> logger.info("获取到的数据：{}", JSON.toJSONString(baseTerminal)));

        Page<BaseTerminal> terminalPage = terminalRepository.findByTerminalCode("TERMINAL", PageRequest.of(1, 200));
        logger.info("当前获取的文档数量：{}，全部文档的数量：{}，获取到的数据：{}", terminalPage.getNumberOfElements(), terminalPage.getTotalElements(), JSON.toJSONString(terminalPage.getContent()));
    }

    @Test
    public void testIndexDatas() {
        boolean indexCreate = terminalRepository.createIndex();
        logger.info("创建索引【{}】", indexCreate ? "成功" : "失败");
        terminalRepository.indexDatas(1000);
    }

    @Test
    public void testSearchData1() {
        List<BaseTerminal> baseTerminals = template.queryForList(new StringQuery("{\"match\":{\"lineNo\":741}}"), BaseTerminal.class);
        logger.info("获取到的数据：{}", JSON.toJSONString(baseTerminals));

        List<BaseTerminal> baseTerminalList = terminalRepository.findByLineNoBetweenOrderByUpdateTimeDesc(700, 800);
        logger.info("获取到的数据：{}", JSON.toJSONString(baseTerminalList));

        Page<BaseTerminal> baseTerminalPage = terminalRepository.findByTerminalCodeAndTerminalNameAndNotLineNo("NUMBER", "第927车", 927, PageRequest.of(0, 200));
        logger.info("当前获取的文档数：{}，全部的文档数：{}，数据内容：{}", baseTerminalPage.getNumberOfElements(), baseTerminalPage.getTotalElements(), JSON.toJSONString(baseTerminalPage.getContent()));

        List<BaseTerminal> baseTerminalList1 = terminalRepository.searchTerminalWithScriptedFieldByScroll(QueryBuilders.matchAllQuery());
        logger.info("获取到的数据：{}", JSON.toJSONString(baseTerminalList1));
    }

}
