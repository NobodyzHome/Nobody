package com.spring.service.repository;

import com.spring.data.domain.BaseLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface LineRepository extends ElasticsearchRepository<BaseLine, String>, LineRepositoryCustom {

    List<BaseLine> findByLineNo(Integer lineNo);

    @Query("{\"match\":{\"lineName\":\"?0\"}}")
    Page<BaseLine> findByLineName(String lineName, Pageable pageable);
}
