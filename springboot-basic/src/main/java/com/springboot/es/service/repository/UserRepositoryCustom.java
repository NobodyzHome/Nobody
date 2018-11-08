package com.springboot.es.service.repository;

import com.springboot.data.domain.BaseUser;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface UserRepositoryCustom {

    Page<BaseUser> findWithScripted(QueryBuilder queryBuilder, Pageable pageable);

    boolean createIndex();

    List<String> indexDatas(int count);

    List<BaseUser> findAllByScroll(QueryBuilder queryBuilder);

    Map<String, Integer> groupByAge(QueryBuilder queryBuilder);
}
