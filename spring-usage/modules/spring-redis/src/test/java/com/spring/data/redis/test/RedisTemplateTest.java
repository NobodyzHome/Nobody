package com.spring.data.redis.test;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.format.Formatter;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.spring.redis.data.domain.BaseDept;
import com.spring.redis.data.domain.BaseLine;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class RedisTemplateTest {

    @Configuration
    static class config {

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration("localhost", 63790);
            serverConfig.setDatabase(0);
            serverConfig.setPassword(RedisPassword.of("Mm500420620"));

            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMinIdle(5);
            poolConfig.setMaxIdle(8);
            poolConfig.setMaxTotal(15);
            poolConfig.setMaxWaitMillis(TimeUnit.SECONDS.toMillis(90));
            poolConfig.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(2));

            JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().clientName("maziqiang")
                    .connectTimeout(Duration.ofSeconds(5)).readTimeout(Duration.ofSeconds(30)).usePooling()
                    .poolConfig(poolConfig).build();

            RedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory(serverConfig, clientConfig);

            return redisConnectionFactory;
        }

        @Bean
        public StringRedisSerializer stringRedisSerializer() {
            StringRedisSerializer stringRedisSerializer = new StringRedisSerializer(Charset.defaultCharset());

            return stringRedisSerializer;
        }

        @Bean
        public FormattingConversionServiceFactoryBean conversionService() {
            FormattingConversionServiceFactoryBean conversionServiceFactoryBean = new FormattingConversionServiceFactoryBean();
            conversionServiceFactoryBean.setRegisterDefaultFormatters(false);
            conversionServiceFactoryBean.setFormatters(Collections.singleton(new Formatter<BaseDept>() {

                @Override
                public String print(BaseDept dept, Locale locale) {
                    StringJoiner joiner = new StringJoiner(",");
                    String contents = joiner.add(dept.getDeptNo().toString()).add(dept.getDeptCode())
                            .add(dept.getDeptName()).toString();

                    return contents;
                }

                @Override
                public BaseDept parse(String contents, Locale locale) throws ParseException {

                    abstract class TokenResolver {

                        public abstract void resolve(BaseDept dept, int index, String token);
                    }

                    class DefaulTokenResolver extends TokenResolver {

                        @Override
                        public void resolve(BaseDept dept, int index, String token) {
                            if (index == 0) {
                                Integer deptNo = Integer.valueOf(token);
                                dept.setDeptNo(deptNo);
                            } else if (index == 1) {
                                dept.setDeptCode(token);
                            } else if (index == 2) {
                                dept.setDeptName(token);
                            }
                        }

                    }

                    TokenResolver tokenResolver = new DefaulTokenResolver();
                    BaseDept dept = new BaseDept();
                    int index = 0;

                    StringTokenizer tokenizer = new StringTokenizer(contents, ",");
                    while (tokenizer.hasMoreTokens()) {
                        tokenResolver.resolve(dept, index++, tokenizer.nextToken());
                    }

                    return dept;
                }
            }));

            return conversionServiceFactoryBean;
        }

        @Bean
        public GenericToStringSerializer<BaseDept> genericToStringSerializer(ConversionService conversionService) {
            GenericToStringSerializer<BaseDept> genericToStringSerializer = new GenericToStringSerializer<BaseDept>(
                    BaseDept.class, Charset.defaultCharset());
            genericToStringSerializer.setConversionService(conversionService);

            return genericToStringSerializer;
        }

        @Bean
        public JdkSerializationRedisSerializer jdkSerializationRedisSerializer() {
            JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();

            return jdkSerializationRedisSerializer;
        }

        @Bean
        public Jackson2JsonRedisSerializer<BaseLine> jackson2JsonRedisSerializer() {
            Jackson2JsonRedisSerializer<BaseLine> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<BaseLine>(
                    BaseLine.class);

            return jackson2JsonRedisSerializer;
        }

        @Bean
        public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
            GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

            return genericJackson2JsonRedisSerializer;
        }

        @Bean
        public RedisTemplate<BaseLine, BaseDept> lineDeptTemplate() {
            RedisTemplate<BaseLine, BaseDept> redisTemplate = new RedisTemplate<>();
            redisTemplate.setKeySerializer(jackson2JsonRedisSerializer());
            /*
             * 由于genericToStringSerializer()
             * 方法是先去容器中寻找名为genericToStringSerializer的bean，
             * 因此不需要给genericToStringSerializer方法传任何参数
             */
            redisTemplate.setValueSerializer(genericToStringSerializer(null));
            redisTemplate.setConnectionFactory(redisConnectionFactory());

            return redisTemplate;
        }

        @Bean
        public RedisTemplate<String, BaseDept> deptTemplate() {
            RedisTemplate<String, BaseDept> redisTemplate = new RedisTemplate<>();
            redisTemplate.setKeySerializer(stringRedisSerializer());
            redisTemplate.setValueSerializer(jdkSerializationRedisSerializer());
            redisTemplate.setConnectionFactory(redisConnectionFactory());

            return redisTemplate;
        }

        @Bean
        public StringRedisTemplate stringRedisTemplate() {
            StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(redisConnectionFactory());

            return stringRedisTemplate;
        }

        @Bean
        public RedisTemplate<Date, BaseDept> dateTemplate() {
            RedisTemplate<Date, BaseDept> template = new RedisTemplate<>();
            template.setConnectionFactory(redisConnectionFactory());
            template.setKeySerializer(jdkSerializationRedisSerializer());
            template.setValueSerializer(genericJackson2JsonRedisSerializer());

            return template;
        }
    }

    @Autowired
    private RedisTemplate<BaseLine, BaseDept> lineDeptTemplate;

    @Autowired
    private RedisTemplate<String, BaseDept> deptTemplate;

    @Autowired
    private StringRedisTemplate stringTemplate;

    @Autowired
    private RedisTemplate<Date, BaseDept> dateTemplate;

    @Test
    public void test1() {
        BaseDept dept = new BaseDept(200, "No:200", "200Dept");
        dept.setModifyDate(new Date());
        dept.setIsRun(true);

        BaseLine line = new BaseLine(13, "No.13", "13line");
        line.setModifyDate(new Date());
        line.setIsRun(false);
        line.setDept(dept);

        BoundListOperations<BaseLine, BaseDept> listOps = lineDeptTemplate.boundListOps(line);
        Long counts = listOps.rightPushAll(dept, new BaseDept(50, "No:50", "50Dept"),
                new BaseDept(15, "No:15", "15Dept"), new BaseDept(210, "No:210", "210Dept"));
        List<BaseDept> listResult = listOps.range(-3, -1);
        BaseDept deptPoped = listOps.leftPop();
        System.out.println(deptPoped.getDeptName());

        List<BaseDept> deptRange = listOps.range(0, -1);
        deptRange.forEach(theDept -> System.out.println(theDept.getDeptName()));

        lineDeptTemplate.expire(line, 2, TimeUnit.MINUTES);
        Long expireTime = lineDeptTemplate.getExpire(line, TimeUnit.SECONDS);
        System.out.println(expireTime);

        lineDeptTemplate.delete(line);
        ValueOperations<BaseLine, BaseDept> opsForValue = lineDeptTemplate.opsForValue();
        opsForValue.setIfAbsent(line, dept);
        BaseDept deptOld = opsForValue.getAndSet(line, new BaseDept(700, "No:700", "700公司"));
        System.out.println(deptOld.getDeptName());
        opsForValue.append(line, " wo ha ha!");

        BaseDept deptGet = opsForValue.get(line);
        System.out.println(deptGet.getDeptName());
    }

    @Test
    @Ignore
    public void test2() {
        deptTemplate.delete(deptTemplate.keys("*"));
        ZSetOperations<String, BaseDept> opsForZSet = deptTemplate.opsForZSet();

        BaseDept dept1 = new BaseDept(50, "No:50", "50公司");
        BaseDept dept2 = new BaseDept(30, "No:30", "30公司");
        BaseDept dept3 = new BaseDept(90, "No:90", "90公司");

        opsForZSet.add("deptSet", dept1, 3.51);
        opsForZSet.add("deptSet", dept2, -0.29);
        opsForZSet.add("deptSet", dept3, -21.311);

        Set<BaseDept> range = opsForZSet.range("deptSet", 0, -1);
        range.forEach(dept -> System.out.println(dept.getDeptName()));

        opsForZSet.add("deptSet1", dept1, 17);
        opsForZSet.add("deptSet1", dept3, 11);
        opsForZSet.add("deptSet1", new BaseDept(88, "No:88", "88公司"), 5.331);

        opsForZSet.unionAndStore("deptSet", "deptSet1", "deptSetUnion");

        opsForZSet.range("deptSetUnion", 0, -1).forEach(dept -> {
            Double score = opsForZSet.score("deptSetUnion", dept);
            Long rank = opsForZSet.rank("deptSetUnion", dept);
            System.out.println(rank + "_" + score + "_" + dept.getDeptName());
        });

    }

    @Test
    @Ignore
    public void test3() {
        HashOperations<String, Object, Object> opsForHash = stringTemplate.opsForHash();

        opsForHash.put("hashDept", "deptNo", 11);
        opsForHash.put("hashDept", "deptCode", "No:11");
        opsForHash.put("hashDept", "deptName", "11公司");
        opsForHash.put("hashDept", "isRun", "true");

        opsForHash.increment("hashDept", "deptNo", 98);
        System.out.println(opsForHash.get("hashDept", "deptNo"));
        opsForHash.entries("hashDept").forEach((key, value) -> System.out.println(key + ":" + value));
        opsForHash.delete("hashDept", "isRun");

        opsForHash.multiGet("hashDept", opsForHash.keys("hashDept")).forEach(System.out::println);
    }

    @Test
    @Ignore
    public void test4() throws ParseException {
        SetOperations<Date, BaseDept> opsForSet = dateTemplate.opsForSet();
        DateFormat dateFormat = DateFormat.getDateTimeInstance();

        BaseDept dept1 = new BaseDept(60, "No:60", "60公司");
        BaseDept dept2 = new BaseDept(70, "No:70", "70公司");
        BaseDept dept3 = new BaseDept(20, "No:20", "20公司");

        dept1.setIsRun(false);
        dept2.setIsRun(true);

        dept3.setModifyDate(dateFormat.parse("2017-11-08 20:30:11"));
        dept2.setModifyDate(dateFormat.parse("2018-05-20 11:23:19"));
        dept1.setModifyDate(dateFormat.parse("2018-10-10 09:40:55"));

        Date date1 = DateFormat.getDateInstance().parse("2018-05-20");
        Date date2 = DateFormat.getDateInstance().parse("2017-10-15");

        opsForSet.add(date1, dept2, dept3, dept2, dept3, dept3, new BaseDept(88, "No:88", "88公司"));
        System.out.println(opsForSet.size(date1));

        opsForSet.add(date2, dept2, new BaseDept(88, "No:88", "88公司"), new BaseDept(51, "No:51", "51公司"));

        opsForSet.members(date1).forEach(dept -> System.out.println(dept.getDeptName()));
        opsForSet.members(date2).forEach(dept -> System.out.println(dept.getDeptName()));

        Date now = new Date();
        opsForSet.differenceAndStore(date1, date2, now);
        opsForSet.members(now).forEach(dept -> System.out.println(dept.getDeptName()));
    }

    @Test
    public void test5() {
        String key = "test";
        BoundValueOperations<String, String> valueOps = stringTemplate.boundValueOps(key);
        valueOps.set("ni hao");

        BoundValueOperations<String, BaseDept> valueOps1 = deptTemplate.boundValueOps(key);
        try {
            /*
             * 根据"ni hao"的二进制数据进行反序列化，当然会报错。
             * jdk中的反序列化的二进制数据只能是ObjectOutputStream写出的数据。
             */
            BaseDept dept = valueOps1.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        valueOps1.set(new BaseDept(99, "No.99", "99公司"));
        // 可以把BaseDept序列化的二进制数据，按照String对象的读取方式来读取成String对象
        String contents = valueOps.get();
        System.out.println(contents);

    }
}