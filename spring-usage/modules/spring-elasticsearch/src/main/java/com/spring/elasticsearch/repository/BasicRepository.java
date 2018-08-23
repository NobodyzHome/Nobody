package com.spring.elasticsearch.repository;

import com.spring.data.domain.CustomerRelation;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BasicRepository extends ElasticsearchRepository<CustomerRelation, String> {

}
