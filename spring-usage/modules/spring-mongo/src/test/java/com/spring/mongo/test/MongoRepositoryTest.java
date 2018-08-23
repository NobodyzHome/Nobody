package com.spring.mongo.test;

import com.google.common.collect.Lists;
import com.spring.mongodb.repository.BaseDeptRepository;
import com.spring.mongodb.repository.BaseLineRepository;
import com.spring.redis.data.domain.BaseDept;
import com.spring.redis.data.domain.BaseLine;
import com.spring.redis.data.domain.DomainUtils;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.*;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-mongo.xml")
@TestPropertySource(properties = {"year=2018", "date=${year}-1-1"})
public class MongoRepositoryTest {

    @Autowired
    private BaseDeptRepository baseDeptRepository;

    @Autowired
    private BaseLineRepository baseLineRepository;

    @Value("${date}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    @Value("${year}-5-21 13:20:55")
    @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
    private Date date1;

    @Value("2019-10-31")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date2;

    @Test
    public void test1() {
//        BaseDept[] baseDepts = DomainUtils.generateDepts(30);
//        baseDeptRepository.saveAll(Lists.newArrayList(baseDepts));

        Integer count = baseDeptRepository.countByIsRunEquals(true);
        System.out.println(count);

        Pageable pageRequest = PageRequest.of(0, 5, Sort.Direction.ASC, "deptNo");
        Page<BaseDept> pageByIsRun;

        do {
            pageByIsRun = baseDeptRepository.findByIsRun(false, pageRequest);
            pageByIsRun.stream().map(BaseDept::getDeptName).forEach(System.out::println);
            if (pageByIsRun.hasNext()) {
                pageRequest = pageByIsRun.nextPageable();
            }
        } while (pageByIsRun.hasNext());

        String[] deptList = baseDeptRepository.findDistinctByDeptCodeIgnoreCaseStartingWith("no:", Sort.by("isRun", "modifyDate"));
        Arrays.stream(deptList).forEach(System.out::println);

        Optional<BaseDept> topBaseDept = baseDeptRepository.findTopByDeptNoBetween(40, 77);
        topBaseDept.ifPresent(dept -> System.out.println(dept.getDeptName()));

        Slice<BaseDept> byDeptNoExists = baseDeptRepository.findByDeptNoContains(Lists.newArrayList(73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 213, 99, 1121, 45)
                , PageRequest.of(0, 20, Sort.Direction.DESC, "deptNo"));
        byDeptNoExists.getContent().forEach(dept -> System.out.println(dept.getDeptName()));

        Page<BaseDept> deptPage = baseDeptRepository.findByModifyDateBeforeAndIsRunEquals(new Date(), false, PageRequest.of(2, 5));
        System.out.println(deptPage.getTotalElements() + ":" + deptPage.getTotalPages());

        List<BaseDept> baseDepts = baseDeptRepository.findFirst10ByIsRunAndModifyDateAfterOrderByDeptNo(true, date);
        baseDepts.forEach(dept -> System.out.println(dept.getDeptName()));

        List<String> deptCodeList = baseDeptRepository.findDeptCodeByModifyDateBetweenAndIsRun(date, date1, false);
        System.out.println(deptCodeList);
    }

    @Test
    public void test2() {
//        BaseLine[] lines = DomainUtils.generateLines(10);
//        List<BaseLine> baseLines = baseLineRepository.saveAll(Lists.newArrayList(lines));

        BaseDept dept1 = DomainUtils.generateDept(878);
        BaseLine baseLine = DomainUtils.generateLine(13, 87);
        baseLine.setDept1(dept1);
        baseLineRepository.save(baseLine);

        List<BaseLine> byDept_isRun = baseLineRepository.findByDept_IsRun(true);
        System.out.println(byDept_isRun);

        Optional<BaseLine> line1 = baseLineRepository.findFirstByDept_IsRunOrderByModifyDate(false);
        line1.ifPresent(line -> System.out.println(line.getLineName()));

        List<Document> lineStream = baseLineRepository.findByModifyDateBefore(date2);
        lineStream.forEach(System.out::println);

        ListenableFuture<Stream<BaseLine>> listenableFuture = baseLineRepository.findByIsRunNotOrderByLineNo(true);
        listenableFuture.addCallback(stream -> {
            System.out.println(Thread.currentThread().getName());
            stream.forEach(line -> System.out.println(line.getLineName()));
        }, Throwable::printStackTrace);

        String jsonContents = baseLineRepository.findByLineNo(13);
        System.out.println(jsonContents);

        Boolean exists1 = baseLineRepository.existsByLineNo(13);
        Boolean exists2 = baseLineRepository.existsByLineNo(15);
        System.out.println(exists1);
        System.out.println(exists2);
    }

    @Test
    public void test3() {
        List<BaseLine> test = baseLineRepository.findTest1(new Date(), true);
        System.out.println(test);

        List<Map<String, Object>> test2 = baseLineRepository.findTest2(true);
        test2.forEach(System.out::println);

        Integer count = baseLineRepository.findCount(date, date2);
        List<BaseLine> delete = baseLineRepository.delete(true);
        System.out.println(count);
        System.out.println(delete);

        Boolean exists = baseLineRepository.exists(13);
        Boolean exists1 = baseLineRepository.exists(15);

        System.out.println(exists);
        System.out.println(exists1);
    }
}
