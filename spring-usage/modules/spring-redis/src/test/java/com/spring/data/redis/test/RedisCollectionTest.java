package com.spring.data.redis.test;

import static com.spring.redis.data.domain.DomainUtils.generateLine;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.DefaultRedisSet;
import org.springframework.data.redis.support.collections.DefaultRedisZSet;
import org.springframework.data.redis.support.collections.RedisCollectionFactoryBean;
import org.springframework.data.redis.support.collections.RedisCollectionFactoryBean.CollectionType;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.data.redis.support.collections.RedisProperties;
import org.springframework.data.redis.support.collections.RedisSet;
import org.springframework.data.redis.support.collections.RedisZSet;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.google.common.collect.Sets;
import com.spring.redis.data.domain.BaseLine;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class RedisCollectionTest {

	@Configuration
	@ImportResource("classpath:dataSource.xml")
	@EnableAsync
	static class Config {

		@Bean
		public RedisConnectionFactory redisConnectionFactory() {
			RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration("localhost", 63790);
			serverConfig.setDatabase(0);
			serverConfig.setPassword(RedisPassword.none());

			GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
			poolConfig.setMinIdle(5);
			poolConfig.setMaxIdle(8);
			poolConfig.setMaxTotal(20);
			poolConfig.setMaxWaitMillis(TimeUnit.SECONDS.toMillis(90));
			poolConfig.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(3));

			JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().clientName("maziqiang")
					.connectTimeout(Duration.ofSeconds(60)).readTimeout(Duration.ofSeconds(30)).usePooling()
					.poolConfig(poolConfig).build();

			RedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory(serverConfig, clientConfig);

			return redisConnectionFactory;
		}

		@Bean
		public ConversionService conversionService() {
			FormattingConversionService conversionService = new FormattingConversionService();
			conversionService.addFormatter(new DateFormatter("yyyy-MM-dd"));

			return conversionService;
		}

		@Bean
		public GenericToStringSerializer<Date> dateToStringSerializer() {
			GenericToStringSerializer<Date> dateToStringSerializer = new GenericToStringSerializer<>(Date.class);
			dateToStringSerializer.setConversionService(conversionService());

			return dateToStringSerializer;
		}

		@Bean
		public GenericJackson2JsonRedisSerializer jacksonSerializer() {
			return new GenericJackson2JsonRedisSerializer();
		}

		@Bean
		public StringRedisSerializer stringSerializer() {
			return new StringRedisSerializer(Charset.defaultCharset());
		}

		@Bean
		public RedisTemplate<String, BaseLine> redisTemplate() {
			RedisTemplate<String, BaseLine> redisTemplate = new RedisTemplate<>();
			redisTemplate.setKeySerializer(stringSerializer());
			redisTemplate.setValueSerializer(jacksonSerializer());
			redisTemplate.setConnectionFactory(redisConnectionFactory());

			return redisTemplate;
		}

		@Bean
		public RedisList<BaseLine> redisList() {
			// 使用DefaultRedisList有一个缺点就是要求RedisTemplate的Key必须是String类型的。
			RedisList<BaseLine> redisList = new DefaultRedisList<BaseLine>("test-list", redisTemplate());

			return redisList;
		}

		@Bean
		public RedisSet<BaseLine> redisSet() {
			RedisSet<BaseLine> redisSet = new DefaultRedisSet<>("test-set", redisTemplate());

			return redisSet;
		}

		@Bean
		public RedisZSet<BaseLine> redisZSet() {
			RedisZSet<BaseLine> redisZSet = new DefaultRedisZSet<BaseLine>("test-zset", redisTemplate());

			return redisZSet;
		}

		@Bean
		public DataSourceTransactionManager transactionManager(DataSource dataSource) {
			DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
			transactionManager.setDataSource(dataSource);

			return transactionManager;
		}

		@Bean
		public TaskExecutor taskExecutor() {
			ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
			taskExecutor.setCorePoolSize(20);
			taskExecutor.setMaxPoolSize(30);
			taskExecutor.setQueueCapacity(3);
			taskExecutor.setRejectedExecutionHandler(new AbortPolicy());
			taskExecutor.setAllowCoreThreadTimeOut(false);
			taskExecutor.setKeepAliveSeconds(30);
			taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
			taskExecutor.setAwaitTerminationSeconds(300);
			taskExecutor.setThreadGroupName("gp");
			taskExecutor.setThreadNamePrefix("task-");

			return taskExecutor;
		}

		@Bean
		public Service<BaseLine> service() {
			return new Service<>(redisList());
		}

		@Bean
		public RedisMap<String, Object> redisMap() {
			RedisMap<String, Object> redisMap = new DefaultRedisMap<>("test-map", redisTemplate());

			return redisMap;
		}

		@Bean
		public RedisProperties redisProperties() {
			RedisProperties redisProperties = new RedisProperties("test-properties", redisTemplate());

			return redisProperties;
		}

		@Bean
		public RedisCollectionFactoryBean redisMap1() {
			RedisCollectionFactoryBean factoryBean = new RedisCollectionFactoryBean();
			factoryBean.setTemplate(redisTemplate());
			factoryBean.setKey("test-map1");
			factoryBean.setType(CollectionType.MAP);

			return factoryBean;
		}

	}

	static class Service<T> {

		private RedisList<T> redisList;

		public Service(RedisList<T> redisList) {
			this.redisList = redisList;
		}

		@Async
		public void put(T line) throws InterruptedException {
			redisList.put(line);
		}

		@Async
		public ListenableFuture<T> take() throws InterruptedException {
			T line = redisList.take();
			return new AsyncResult<>(line);
		}
	}

	@Autowired
	private RedisTemplate<String, BaseLine> redisTemplate;

	@Autowired
	private RedisList<BaseLine> redisList;

	@Autowired
	private RedisSet<BaseLine> redisSet;

	@Autowired
	private RedisZSet<BaseLine> redisZSet;

	@Autowired
	private Service<BaseLine> service;

	@Autowired
	private RedisMap<String, Object> redisMap;

	@Autowired
	private RedisProperties redisProperties;

	@Autowired
	private DefaultRedisMap<String, Object> redisMap1;

	@Test
	@Transactional
	@Commit
	@Ignore
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	public void test1() {
		redisTemplate.setEnableTransactionSupport(true);

		redisList.addFirst(generateLine(609, 13));
		redisList.addFirst(generateLine(911, 5));
		redisList.add(generateLine(50, 3));
		redisList.addLast(generateLine(879, 2));
		redisList.add(generateLine(872, 3));

		BaseLine line = redisList.get(1);
		System.out.println(line.getLineName());

		redisList.remove(line);

		List<BaseLine> allLine = redisList.range(0, -1);
		allLine.forEach(theLine -> System.out.println(theLine.getLineName()));

		List<BaseLine> trimedList = redisList.trim(-4, -2).range(0, -1);
		trimedList.forEach(theLine -> System.out.println(theLine.getLineName()));
	}

	@Test
	@Ignore
	public void test2() {
		BaseLine line1 = generateLine(572, 1);
		BaseLine line2 = generateLine(899, 3);
		BaseLine line3 = generateLine(263, 1);
		BaseLine line4 = generateLine(5, 8);
		BaseLine line5 = generateLine(872, 9);
		BaseLine line6 = generateLine(266, 5);
		BaseLine line7 = generateLine(40, 5);
		BaseLine line8 = generateLine(366, 10);
		BaseLine line9 = generateLine(50, 11);

		redisSet.add(line1);
		redisSet.addAll(Sets.newHashSet(line2, line8, line9, line5, line7));
		redisSet.forEach(line -> System.out.println(line.getLineName()));

		RedisSet<BaseLine> redisSet1 = new DefaultRedisSet<>("test-set-1", redisTemplate);
		redisSet1.addAll(Sets.newHashSet(line3, line4, line9, line8, line1));

		RedisSet<BaseLine> redisSet2 = new DefaultRedisSet<>("test-set-2", redisTemplate);
		redisSet2.addAll(Sets.newHashSet(line6, line2, line8, line1));

		// 带*Store的方法的返回结果是一个新的RedisSet对象，这个RedisSet对象操纵的是新存储的key。例如redisSetDiff变量操纵的是test-set-diff这个key
		RedisSet<BaseLine> redisSetDiff = redisSet.diffAndStore(Sets.newHashSet(redisSet1, redisSet2), "test-set-diff");
		RedisSet<BaseLine> redisSetIntersect = redisSet.intersectAndStore(Sets.newHashSet(redisSet1, redisSet2),
				"test-set-intersect");
		RedisSet<BaseLine> redisSetUnion = redisSet.unionAndStore(Sets.newHashSet(redisSet1, redisSet2),
				"test-set-union");

		redisSetDiff.forEach(line -> System.out.println(line.getLineName()));
		redisSetIntersect.forEach(line -> System.out.println(line.getLineName()));
		redisSetUnion.forEach(line -> System.out.println(line.getLineName()));

		redisSet.remove(line2);
	}

	@Test
	@Ignore
	public void test3() {
		BaseLine line1 = generateLine(572, 1);
		BaseLine line2 = generateLine(899, 3);
		BaseLine line3 = generateLine(263, 1);
		BaseLine line4 = generateLine(5, 8);
		BaseLine line5 = generateLine(872, 9);
		BaseLine line6 = generateLine(266, 5);
		BaseLine line7 = generateLine(40, 5);
		BaseLine line8 = generateLine(366, 10);
		BaseLine line9 = generateLine(50, 11);

		redisZSet.add(line1, 35.28);
		redisZSet.add(line2, -10.55);
		redisZSet.add(line3, -0.215);
		redisZSet.add(line4, 10.25);
		redisZSet.add(line8, 31);

		RedisZSet<BaseLine> redisZSet1 = new DefaultRedisZSet<BaseLine>("test-zset-1", redisTemplate, 15);
		redisZSet1.add(line3);
		redisZSet1.add(line4, 2.97);
		redisZSet1.add(line6, -20.557);
		redisZSet1.add(line9, -40.89);
		redisZSet1.add(line5);

		RedisZSet<BaseLine> redisZSet2 = new DefaultRedisZSet<BaseLine>("test-zset-2", redisTemplate);
		redisZSet2.add(line3, 12);
		redisZSet2.add(line4, -27);
		redisZSet2.add(line7);

		Double line1Score = redisZSet.score(line1);
		redisZSet.remove(line8);

		RedisZSet<BaseLine> redisZSetUnion = redisZSet.unionAndStore(Sets.newHashSet(redisZSet1, redisZSet2),
				"test-zset-union");
		RedisZSet<BaseLine> redisZSetIntersect = redisZSet.intersectAndStore(Sets.newHashSet(redisZSet1, redisZSet2),
				"test-zset-intersect");

		Set<BaseLine> rangeByScore = redisZSet.reverseRangeByScore(5, 40);
		rangeByScore.forEach(line -> System.out.println(line.getLineName() + ":" + redisZSet.score(line)));

		redisZSetUnion.range(0, -1)
				.forEach(line -> System.out.println(line.getLineName() + ":" + redisZSetUnion.score(line)));
		redisZSetIntersect.range(0, -1)
				.forEach(line -> System.out.println(line.getLineName() + ":" + redisZSetIntersect.score(line)));

		redisZSet.removeByScore(-100, 0)
				.forEach(line -> System.out.println(line.getLineName() + ":" + redisZSet.score(line)));
	}

	@Test
	@Repeat(9)
	@Ignore
	public void test4() throws InterruptedException {
		service.put(generateLine());
	}

	@Test
	@Repeat(5)
	@Ignore
	public void test5() throws InterruptedException {
		ListenableFuture<BaseLine> future = service.take();
		future.addCallback(line -> {
			System.out.println(line.getLineName());
		}, Throwable::printStackTrace);
	}

	@Test
	@Ignore
	public void test6() {
		while (true) {
		}
	}

	@Test
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	@Ignore
	public void test7() {
		redisTemplate.setHashKeySerializer(new StringRedisSerializer(Charset.defaultCharset()));
		redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

		Jackson2HashMapper hashMapper = new Jackson2HashMapper(false);
		BaseLine line = generateLine(919, 70);
		Map<String, Object> hash = hashMapper.toHash(line);
		redisMap.putAll(hash);

		redisMap.increment("lineNo", 50);

		Object dept = redisMap.get("dept");
		Object lineCode = redisMap.remove("lineCode");

		Object lineFromRedis = hashMapper.fromHash(redisMap);
		BaseLine line1 = (BaseLine) lineFromRedis;
		System.out.println(line1.getLineName() + ":" + line1.getDept().getDeptName());

	}

	@SuppressWarnings("unused")
	@Test
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	@Ignore
	public void test8() {
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new StringRedisSerializer());

		// 使用Properties的方法
		redisProperties.setProperty("zhangsan", "张三");
		redisProperties.setProperty("lisi", "李四");
		// 使用Map的方法
		redisProperties.put("user", "maziqiang");

		// 使用Properties的方法
		/*
		 * 此处可以是RedisProperties的bug，
		 * RedisProperties的getProperty方法并没有从redis数据库中获取数据，
		 * 而是直接使用父类Properties的getProperty方法。因此，该方法没有从redis中获取数据，因此该方法返回了null。
		 * 
		 * 该问题可以通过使用Map的get方法来解决
		 */
		String zhangsan = redisProperties.getProperty("zhangsan");
		String lisi = redisProperties.getProperty("lisi");

		// 使用Map的方法
		// 使用Map的get方法能解决使用RedisProperties的getProperty方法无法从redis中获取数据的问题
		Object user1 = redisProperties.get("user");
		Object user = redisProperties.remove("user");

		Properties properties = new Properties(redisProperties);
		properties.forEach((key, value) -> System.out.println(key + ":" + value));
	}

	@Test
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	public void test9() {
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

		redisMap1.put("hello", "world");
		redisMap1.put("user", "maziqiang");
		redisMap1.put("nice", new Date());
		redisMap1.put("qq", 291);
		redisMap1.put("table", generateLine());

		Collection<Object> values = redisMap1.values();
		values.forEach(System.out::println);

		Object removed = redisMap1.remove("nice");
		assert removed instanceof Date;
	}

}
