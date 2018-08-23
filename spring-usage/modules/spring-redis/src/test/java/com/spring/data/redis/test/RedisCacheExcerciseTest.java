package com.spring.data.redis.test;

import static com.spring.redis.data.domain.DomainUtils.generateDept;
import static com.spring.redis.data.domain.DomainUtils.generateDepts;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.NamedCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.DefaultRedisSet;
import org.springframework.data.redis.support.collections.DefaultRedisZSet;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.data.redis.support.collections.RedisProperties;
import org.springframework.data.redis.support.collections.RedisSet;
import org.springframework.data.redis.support.collections.RedisZSet;
import org.springframework.format.Formatter;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.spring.redis.data.domain.BaseDept;
import com.spring.redis.data.domain.BaseLine;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class RedisCacheExcerciseTest {

	@Configuration
	@ImportResource("classpath:dataSource.xml")
	@EnableCaching(order = -10)
	@EnableTransactionManagement(order = -20)
	@EnableAspectJAutoProxy
	static class Config {

		@Bean
		public RedisConnectionFactory connFactory() {
			RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration("localhost", 63790);
			serverConfig.setDatabase(0);

			GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
			poolConfig.setMinIdle(3);
			poolConfig.setMaxIdle(5);
			poolConfig.setMaxTotal(10);
			poolConfig.setMaxWaitMillis(TimeUnit.MINUTES.toMillis(2));
			poolConfig.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(5));

			JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().clientName("maziqiang")
					.connectTimeout(Duration.ofSeconds(90)).readTimeout(Duration.ofSeconds(5)).usePooling()
					.poolConfig(poolConfig).build();

			RedisConnectionFactory connFactory = new JedisConnectionFactory(serverConfig, clientConfig);

			return connFactory;
		}

		@Bean
		public StringRedisSerializer stringRedisSerializer() {
			return new StringRedisSerializer();
		}

		@Bean
		public JdkSerializationRedisSerializer jdkRedisSerializer() {
			return new JdkSerializationRedisSerializer();
		}

		@Bean
		public GenericJackson2JsonRedisSerializer jacksonRedisSerializer() {
			return new GenericJackson2JsonRedisSerializer();
		}

		@Bean(autowire = Autowire.BY_TYPE)
		public GenericToStringSerializer<BaseDept> deptToStringSerializer() {
			GenericToStringSerializer<BaseDept> serializer = new GenericToStringSerializer<>(BaseDept.class);

			return serializer;
		}

		@SuppressWarnings("unchecked")
		@Bean
		public FormattingConversionServiceFactoryBean conversionService() {
			FormattingConversionServiceFactoryBean conversionFactory = new FormattingConversionServiceFactoryBean();
			conversionFactory.setRegisterDefaultFormatters(true);
			conversionFactory.setFormatters(Sets.newHashSet(new DateFormatter("yyyy-MM-dd"), new Formatter<BaseDept>() {

				private final String DELIMITER = ",";

				@Override
				public String print(BaseDept dept, Locale locale) {
					StringJoiner joiner = new StringJoiner(DELIMITER);
					String contents = joiner.add(dept.getDeptNo().toString()).add(dept.getDeptCode())
							.add(dept.getDeptName()).toString();

					return contents;
				}

				@Override
				public BaseDept parse(String text, Locale locale) throws ParseException {

					StringTokenizer tokenizer = new StringTokenizer(text, DELIMITER);
					BaseDept dept = new BaseDept();

					for (int index = 0; tokenizer.hasMoreElements(); index++) {
						String token = tokenizer.nextToken();

						if (index == 0) {
							dept.setDeptNo(Integer.valueOf(token));
						} else if (index == 1) {
							dept.setDeptCode(token);
						} else if (index == 2) {
							dept.setDeptName(token);
						}
					}

					return dept;
				}

			}));

			return conversionFactory;
		}

		@Bean
		public CacheManager cacheManager(ConversionService conversionService) {
			RedisCacheWriter cacheWriter = RedisCacheWriter.lockingRedisCacheWriter(connFactory());

			RedisCacheConfiguration helloConfig = RedisCacheConfiguration.defaultCacheConfig()
					.entryTtl(Duration.ofSeconds(90)).withConversionService(conversionService)
					.serializeKeysWith(SerializationPair.fromSerializer(stringRedisSerializer()))
					.serializeValuesWith(SerializationPair.fromSerializer(jdkRedisSerializer()));

			RedisCacheConfiguration worldConfig = RedisCacheConfiguration.defaultCacheConfig()
					.entryTtl(Duration.ofMinutes(2))
					.serializeValuesWith(SerializationPair.fromSerializer(jacksonRedisSerializer()));

			RedisCacheConfiguration userConfig = RedisCacheConfiguration.defaultCacheConfig()
					.entryTtl(Duration.ofMinutes(5)).withConversionService(conversionService)
					.serializeValuesWith(SerializationPair.fromSerializer(jacksonRedisSerializer()));

			RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
					.disableCachingNullValues().entryTtl(Duration.ZERO)
					.serializeValuesWith(SerializationPair.fromSerializer(jacksonRedisSerializer()));

			Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
			configMap.put("hello", helloConfig);
			configMap.put("world", worldConfig);
			configMap.put("user", userConfig);

			RedisCacheManager cacheManager = RedisCacheManager.builder(cacheWriter).cacheDefaults(defaultConfig)
					.initialCacheNames(Sets.newHashSet("hello", "world", "user", "test", "chatroom"))
					.withInitialCacheConfigurations(configMap).transactionAware().build();

			return cacheManager;
		}

		@Bean
		public RedisTemplate<String, BaseDept> redisTemplate() {
			RedisTemplate<String, BaseDept> redisTemplate = new RedisTemplate<>();
			redisTemplate.setConnectionFactory(connFactory());

			redisTemplate.setKeySerializer(stringRedisSerializer());
			redisTemplate.setValueSerializer(deptToStringSerializer());
			redisTemplate.setHashKeySerializer(stringRedisSerializer());
			redisTemplate.setHashValueSerializer(jacksonRedisSerializer());

			redisTemplate.setEnableTransactionSupport(true);

			return redisTemplate;
		}

		@Bean
		public RedisList<BaseDept> redisList() {
			RedisList<BaseDept> redisList = new DefaultRedisList<BaseDept>("test-list", redisTemplate());

			return redisList;
		}

		@Bean
		public RedisSet<BaseDept> redisSet() {
			RedisSet<BaseDept> redisSet = new DefaultRedisSet<BaseDept>("test-set", redisTemplate());

			return redisSet;
		}

		@Bean
		public RedisZSet<BaseDept> redisZSet() {
			RedisZSet<BaseDept> redisZSet = new DefaultRedisZSet<BaseDept>("test-zset", redisTemplate(), -1);

			return redisZSet;
		}

		@Bean
		public RedisMap<String, Object> redisMap() {
			RedisMap<String, Object> redisMap = new DefaultRedisMap<String, Object>("test-map", redisTemplate());

			return redisMap;
		}

		@Bean
		public RedisProperties redisProperties() {
			RedisProperties redisProperties = new RedisProperties("test-properties", redisTemplate());

			return redisProperties;
		}

		@Bean
		public CacheService cacheService() {
			return new CacheService();
		}

		@Bean
		public ServiceLogger serviceLogger() {
			return new ServiceLogger();
		}

		@Bean
		public CacheResolver cacheResolver(CacheManager cacheManager) {
			NamedCacheResolver cacheResolver = new NamedCacheResolver(cacheManager,
					cacheManager.getCacheNames().toArray(new String[0]));

			return cacheResolver;
		}

	}

	@CacheConfig(cacheNames = { "hello", "chatroom" })
	private static class CacheService {

		@Autowired
		private RedisTemplate<String, BaseDept> redisTemplate;

		@Cacheable
		@Transactional
		public BaseDept service1(Integer deptNo) {
			if (redisTemplate.hasKey(deptNo.toString())
					&& redisTemplate.type(deptNo.toString()).equals(DataType.STRING)) {
				return redisTemplate.opsForValue().get(deptNo.toString());
			} else {
				BaseDept dept = generateDept(deptNo);
				return dept;
			}
		}

		@Cacheable({ "hello", "world", "user" })
		public BaseDept service2(Date modifyDate) {
			BaseDept dept = generateDept(2);
			dept.setModifyDate(modifyDate);

			return dept;
		}

		@Cacheable(cacheResolver = "cacheResolver")
		public BaseLine service3(int lineNo, String lineCode, String lineName, Date modifyDate) {
			BaseLine line = new BaseLine(lineNo, lineCode, lineName);
			line.setModifyDate(modifyDate);
			line.setIsRun(false);
			line.setDept(generateDept(77));

			return line;
		}
	}

	@Aspect
	private static class ServiceLogger {

		@Pointcut("bean(cacheService) && @annotation(org.springframework.cache.annotation.Cacheable)")
		public void serviceMethods() {
		}

		@AfterReturning(pointcut = "serviceMethods()", returning = "result", argNames = "result")
		public void logResult(JoinPoint jp, Object result) {
			System.out.println(jp.getSignature() + ":" + result);
		}
	}

	@Autowired
	private CacheService cacheService;

	@Resource
	private RedisList<BaseDept> redisList;

	@Resource
	private RedisSet<BaseDept> redisSet;

	@Resource
	private RedisZSet<BaseDept> redisZSet;

	@Resource
	private RedisMap<String, Object> redisMap;

	@Resource
	private RedisProperties redisProperties;

	@Resource
	private RedisTemplate<String, BaseDept> redisTemplate;

	@SuppressWarnings("unused")
	@Test
	public void test1() throws ParseException {
		BaseDept dept1 = cacheService.service1(30);
		BaseDept dept2 = cacheService.service1(30);
		BaseDept dept3 = cacheService.service1(5);

		Date date1 = DateFormat.getDateInstance().parse("2018-11-3");
		Date date2 = DateFormat.getDateInstance().parse("2018-3-5");

		BaseDept dept4 = cacheService.service2(date1);
		BaseDept dept5 = cacheService.service2(date1);
		BaseDept dept6 = cacheService.service2(date1);
		BaseDept dept7 = cacheService.service2(date2);
		BaseDept dept8 = cacheService.service2(date2);
		
		BaseLine line1 = cacheService.service3(88, "No:88", "88Line", date1);
		BaseLine line2 = cacheService.service3(88, "No:88", "88Line", date1);
		BaseLine line3 = cacheService.service3(88, "No:88", "88Line", date1);
		BaseLine line4 = cacheService.service3(878, "No:878", "878Line", date2);
		BaseLine line5 = cacheService.service3(878, "No:878", "878Line", date2);
		
	}

	@Test
	public void test2() throws ParseException {
		redisList.offer(generateDept(50));
		redisList.offerFirst(generateDept(30));
		redisList.addLast(generateDept(5));
		BaseDept dept = redisList.get(1);
		System.out.println(dept.getDeptName());

		redisSet.addAll(Sets.newHashSet(generateDepts(30, 90, 20, 50, 15, 3, 6)));
		redisSet.expireAt(DateUtils.addMinutes(new Date(), 2));
		redisSet.add(generateDept(99));
		System.out.println(redisSet.size());

		redisZSet.add(generateDept(9), -31);
		redisZSet.add(generateDept(20), 15);
		redisZSet.add(generateDept(80));
		redisZSet.add(generateDept(7), 2.349);
		redisZSet.add(generateDept(30), 12.116);

		Set<BaseDept> elements = redisZSet.rangeByScore(0, 20);
		elements.forEach(e -> System.out.println(redisZSet.score(e) + ":" + redisZSet.rank(e)));

		Jackson2HashMapper hashMapper = new Jackson2HashMapper(false);
		Map<String, Object> hash = hashMapper.toHash(generateDept());
		redisMap.putAll(hash);
		redisMap.increment("deptNo", 370);
		redisMap.remove("modifyDate");

		BaseDept dept1 = (BaseDept) hashMapper.fromHash(redisMap);
		System.out.println(dept1.getDeptName());

		redisProperties.setProperty("ZS", "zhangsan");
		redisProperties.setProperty("WW", "wangwu");
		redisProperties.setProperty("ZL", "zhaoliu");

	}

}
