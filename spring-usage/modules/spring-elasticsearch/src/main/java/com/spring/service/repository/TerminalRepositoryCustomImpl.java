package com.spring.service.repository;

import com.spring.data.domain.BaseTerminal;
import com.spring.data.domain.generator.DomainGenerator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ScrolledPage;
import org.springframework.data.elasticsearch.core.query.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TerminalRepositoryCustomImpl implements TerminalRepositoryCustom {

    private static Logger logger = LoggerFactory.getLogger(TerminalRepositoryCustom.class);

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public void indexDatas(Integer count) {
        List<BaseTerminal> baseTerminals = DomainGenerator.generateTerminals(count);
        String idCollect = baseTerminals.stream().map(baseTerminal -> new IndexQueryBuilder().withObject(baseTerminal).build())
                .map(elasticsearchTemplate::index).collect(Collectors.joining(","));

        logger.info("已索引的文档的ID为：{}", idCollect);
    }

    @Override
    public boolean createIndex() {
        if (elasticsearchTemplate.indexExists(BaseTerminal.class)) {
            boolean deleteIndex = elasticsearchTemplate.deleteIndex(BaseTerminal.class);
            logger.debug("由于索引已存在，因此先删除索引。删除索引【{}】", deleteIndex ? "成功" : "失败");
        }

        boolean createIndex = elasticsearchTemplate.createIndex(BaseTerminal.class);
        boolean createMapping = elasticsearchTemplate.putMapping(BaseTerminal.class);
        logger.debug("创建索引【{}】", createIndex ? "成功" : "失败");
        logger.debug("创建映射【{}】", createMapping ? "成功" : "失败");

        return createIndex && createMapping;
    }

    @Override
    public List<BaseTerminal> searchTerminalWithScriptedFieldByScroll(QueryBuilder queryBuilder) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryBuilder).withScriptField(new ScriptField("isUpdated", new Script("doc.updateTime.date.isAfter(doc.createTime.date)")))
                .withScriptField(new ScriptField("mLineNo", new Script("doc.lineNo.value * 2"))).withSourceFilter(new FetchSourceFilterBuilder().withIncludes("*").build())
                .withPageable(PageRequest.of(0, 50)).build();

        ScrolledPage<BaseTerminal> baseTerminalPage = (ScrolledPage) elasticsearchTemplate.startScroll(TimeUnit.MINUTES.toMillis(2), searchQuery, BaseTerminal.class);
        List<BaseTerminal> baseTerminalList = new ArrayList<>(Long.valueOf(baseTerminalPage.getTotalElements()).intValue());
        String scrollId;

        do {
            baseTerminalList.addAll(baseTerminalPage.getContent());
            scrollId = baseTerminalPage.getScrollId();
            logger.debug("scrollId为：{}，当前获取的文档数量：{}，全部的文档数量：{}", scrollId, baseTerminalPage.getNumberOfElements(), baseTerminalPage.getTotalElements());
            baseTerminalPage = (ScrolledPage) elasticsearchTemplate.continueScroll(scrollId, TimeUnit.MINUTES.toMillis(2), BaseTerminal.class);
        } while (!baseTerminalPage.getContent().isEmpty());
        elasticsearchTemplate.clearScroll(scrollId);

        return baseTerminalList;
    }
}
