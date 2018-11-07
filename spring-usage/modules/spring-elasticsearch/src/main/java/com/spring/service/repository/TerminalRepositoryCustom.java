package com.spring.service.repository;

import com.spring.data.domain.BaseTerminal;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.List;

public interface TerminalRepositoryCustom {

    void indexDatas(Integer count);

    boolean createIndex();

    List<BaseTerminal> searchTerminalWithScriptedFieldByScroll(QueryBuilder queryBuilder);
}
