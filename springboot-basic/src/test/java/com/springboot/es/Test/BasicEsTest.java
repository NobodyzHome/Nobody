package com.springboot.es.Test;

import com.alibaba.fastjson.JSON;
import com.springboot.MyAppConfig;
import com.springboot.data.domain.BaseUser;
import com.springboot.es.service.repository.UserRepository;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MyAppConfig.class)
public class BasicEsTest {

    private static Logger logger = LoggerFactory.getLogger(BasicEsTest.class);

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeClass
    public static void before() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    @Test
    public void test1() {
        if (elasticsearchTemplate.indexExists(BaseUser.class)) {
            elasticsearchTemplate.deleteIndex(BaseUser.class);
        }

        elasticsearchTemplate.createIndex(BaseUser.class);
        elasticsearchTemplate.putMapping(BaseUser.class);
    }

    @Test
    public void test2() {
        BaseUser user = new BaseUser();
        user.setId("123");
        user.setFirstName("张");
        user.setCode("zhang san");
        user.setPassword("598869");
        user.setIsValid(true);
        user.setCreateTime(new Date());
        user.setAge(RandomUtils.nextInt(5, 25));
        user.setUpdateTime(DateUtils.addDays(user.getCreateTime(), user.getAge()));
        user.setBirthDay(new Date());
        user.setSecondName("三");

        user = userRepository.index(user);
        System.out.println(user.getId());
    }

    @Test
    public void test3() {
        boolean createIndex = userRepository.createIndex();
        logger.info("创建索引：【{}】", createIndex ? "成功" : "失败");

        List<String> idIndexed = userRepository.indexDatas(1000);
        logger.info("插入的数据：{}", idIndexed);
    }

    @Test
    public void test4() {
        QueryBuilder queryBuilder = QueryBuilders.rangeQuery("age").gte(1).lte(30);
        Page<BaseUser> baseUserPage = userRepository.findWithScripted(queryBuilder, PageRequest.of(0, 10));
        logger.info(String.format("当前获取的数据：%s，全部数据：%s，数据内容：%s", baseUserPage.getNumberOfElements(), baseUserPage.getTotalElements(), JSON.toJSONString(baseUserPage)));

        List<BaseUser> baseUserList = userRepository.findByCustom("1985-11-07", 10, 50, "abc");
        logger.info("获取到的数据：{}", JSON.toJSONString(baseUserList));

        List<BaseUser> baseUserList1 = userRepository.findAllByScroll(QueryBuilders.matchAllQuery());
        logger.info("获取到的数据：{}", JSON.toJSONString(baseUserList1));

        Map<String, Integer> groupByAge = userRepository.groupByAge(QueryBuilders.matchAllQuery());
        logger.info("获取到的数据：{}", JSON.toJSONString(groupByAge));
    }


}
