package com.spring.mongodb.repository;

import com.spring.redis.data.domain.BaseDept;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BaseDeptRepository extends MongoRepository<BaseDept, Integer> {

    Integer countByIsRunEquals(boolean flag);

    Optional<BaseDept> findTopByDeptNoBetween(Integer start, Integer end);

    List<BaseDept> findFirst10ByIsRunAndModifyDateAfterOrderByDeptNo(Boolean isRun, Date modifyDate);

    // 如果返回值类型是Page，那么必须有一个Pageable参数s
    Page<BaseDept> findByIsRun(Boolean isRun, Pageable pageable);

    Slice<BaseDept> findByDeptNoContains(List<Integer> deptNo, Pageable pageable);

    // todo 测试
    // 虽然在方法名上也能写OrderBy，但是这样的OrderBy是固定的，不如传入Sort参数，这样排序是动态的
    String[] findDistinctByDeptCodeIgnoreCaseStartingWith(String prefix, Sort sort);

    Page<BaseDept> findByModifyDateBeforeAndIsRunEquals(Date date, Boolean isRun, Pageable pageable);

    List<String> findDeptCodeByModifyDateBetweenAndIsRun(Date start, Date end, Boolean isRun);
}
