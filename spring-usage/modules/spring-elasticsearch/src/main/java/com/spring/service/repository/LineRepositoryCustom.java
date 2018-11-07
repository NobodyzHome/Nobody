package com.spring.service.repository;

import com.spring.data.domain.BaseLine;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface LineRepositoryCustom {
    boolean createIndex(Class clazz);

    List<BaseLine> scrollAll(QueryBuilder queryBuilder);

    List<BaseLine> searchAfterAll(QueryBuilder queryBuilder);

    Map<String, Integer> histogramByLineNo(QueryBuilder queryBuilder);

    Map<Date, Integer> dateHistogramByUpdateTime(QueryBuilder queryBuilder);
}
