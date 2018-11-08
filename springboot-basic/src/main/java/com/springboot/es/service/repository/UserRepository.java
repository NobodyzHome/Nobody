package com.springboot.es.service.repository;

import com.springboot.data.domain.BaseUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface UserRepository extends ElasticsearchRepository<BaseUser, String>, UserRepositoryCustom {

    List<BaseUser> findByAgeAfterAndIsValidFalse(Integer age);

    Page<BaseUser> findByFirstNameAndSecondName(String firstName, String secondName, Pageable pageable);

    @Query("{\"bool\":{\"must\":[{\"match\":{\"birthDay\":\"?0\"}},{\"range\":{\"age\":{\"gte\":?1,\"lt\":?2}}}],\"must_not\":[{\"match\":{\"code\":\"?3\"}}]}}")
    List<BaseUser> findByCustom(String birthDay, Integer ageStart, Integer ageEnd, String notCode);
}
