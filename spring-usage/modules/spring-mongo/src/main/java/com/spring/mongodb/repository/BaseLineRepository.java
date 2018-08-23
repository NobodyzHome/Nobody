package com.spring.mongodb.repository;

import com.spring.redis.data.domain.BaseLine;
import org.bson.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface BaseLineRepository extends MongoRepository<BaseLine, Integer> {
    List<BaseLine> findByDept_IsRun(Boolean flag);

    Optional<BaseLine> findFirstByDept_IsRunOrderByModifyDate(Boolean flag);

    List<Document> findByModifyDateBefore(Date date);

    @Async
    ListenableFuture<Stream<BaseLine>> findByIsRunNotOrderByLineNo(Boolean flag);

    String findByLineNo(Integer lineNo);

    Boolean existsByLineNo(Integer lineNo);

    @Query("{modifyDate:{$gt:?0},isRun:?1}")
    List<BaseLine> findTest1(Date date, Boolean isRun);

    // inclusion and exclusion不能混合用
    @Query(value = "{isRun:?0}", fields = "{_id:1,lineCode:1,modifyDate:1}")
    List<Map<String, Object>> findTest2(Boolean isRun);

    @Query(value = "{modifyDate:{$gt:?0,$lt:?1}}", count = true)
    Integer findCount(Date date1, Date date2);

    // 注意：即使加了delete=true，delete方法返回的也是一个符合@Query查询条件的list，只不过这些数据在mongodb中都被删除了
    @Query(value = "{isRun:?0}", delete = true)
    List<BaseLine> delete(Boolean isRun);

    @Query(value = "{_id:?0}", exists = true)
    Boolean exists(Integer lineNo);
}
