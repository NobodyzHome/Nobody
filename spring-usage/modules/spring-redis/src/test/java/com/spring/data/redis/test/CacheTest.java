package com.spring.data.redis.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.NamedCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.google.common.collect.Sets;
import com.spring.redis.aop.aspect.CacheOperationLogger;
import com.spring.redis.data.domain.BaseDept;
import com.spring.redis.data.domain.BaseLine;
import com.spring.redis.data.service.CacheService;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class CacheTest {

	@Configuration
	@ComponentScan("com.spring.redis.data.service")
	@EnableCaching
	@EnableAsync
	@EnableScheduling
	@EnableAspectJAutoProxy
	@ImportResource("classpath:dataSource.xml")
	static class Config {

		private int dbIndex = 0;

		@Bean
		@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public RedisConnectionFactory connectionFactory() {
			RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration("localhost", 63790);
			serverConfig.setDatabase(dbIndex++);

			GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
			poolConfig.setMinIdle(3);
			poolConfig.setMaxIdle(5);
			poolConfig.setMaxTotal(10);
			poolConfig.setMaxWaitMillis(TimeUnit.SECONDS.toMillis(3));
			poolConfig.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(2));

			JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().clientName("maziqiang")
					.usePooling().poolConfig(poolConfig).build();

			RedisConnectionFactory connectionFactory = new JedisConnectionFactory(serverConfig, clientConfig);
			return connectionFactory;
		}

		@Bean
		/*
		 *
		 * 由于现在有两个CacheManager了，而且@EnableCaching是按类型来从容器中选取默认的CacheManager的@Bean
		 * ，因此需要设置一个@Primary，指定这两个CacheManager的@Bean，哪个是默认的CacheManager
		 *
		 *
		 * 默认的CacheManager用于，如果@Cacheable中没有给出cacheManager属性，则使用默认的CacheManager。
		 */
		@Primary
		public CacheManager cacheManager1() {
			RedisCacheWriter cacheWriter = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory());

			RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
					.entryTtl(Duration.ofMinutes(5));

			RedisCacheManager cacheManager = RedisCacheManager.builder(cacheWriter).cacheDefaults(defaultCacheConfig)
					.initialCacheNames(Sets.newHashSet("hello", "world")).build();

			return cacheManager;
		}

		@Bean
		public CacheManager cacheManager2() {

			RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
					.entryTtl(Duration.ofMinutes(2))
					.serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

			RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory()).cacheDefaults(defaultConfig)
					.initialCacheNames(Sets.newHashSet("dept", "user")).build();

			return cacheManager;
		}

		@Bean
		public CacheManager cacheManager3() {
			RedisCacheWriter redisCacheWriter = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory());

			RedisCacheConfiguration testCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
					.entryTtl(Duration.ofMinutes(2))
					.serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

			RedisCacheConfiguration chatroomCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
					.entryTtl(Duration.ofSeconds(60));

			RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
					.disableCachingNullValues()
					.serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

			ExpressionParser parser = new SpelExpressionParser();
			Expression expression = parser.parseExpression("{'test':#testCacheConfig,'chatroom':#chatroomCacheConfig}");
			EvaluationContext context = new StandardEvaluationContext();
			context.setVariable("testCacheConfig", testCacheConfig);
			context.setVariable("chatroomCacheConfig", chatroomCacheConfig);

			@SuppressWarnings("unchecked")
			Map<String, RedisCacheConfiguration> map = expression.getValue(context, Map.class);

			RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisCacheWriter)
					.cacheDefaults(defaultCacheConfig).initialCacheNames(Sets.newHashSet("test", "chatroom", "tmp"))
					.withInitialCacheConfigurations(map).build();

			return redisCacheManager;
		}

		@Bean
		public CacheResolver cacheResolver() {
			CacheResolver cacheResolver = new NamedCacheResolver(cacheManager3(),
					cacheManager3().getCacheNames().toArray(new String[0]));

			return cacheResolver;
		}

		@Bean
		public CacheManager cacheManager4() {
			/*
			 * 如果RedisCacheManager的transactionAware属性为true，
			 * 那么这个RedisCacheManager创建出来的则是TransactionAwareCacheDecorator，它的put、
			 * remove方法的实现是注册一个事务任务，这个事务任务在事务提交时才会被执行。保证了只有在事务提交后，
			 * 才真正会向RedisCache执行remove、put等操作。
			 */
			RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory())
					.initialCacheNames(Collections.singleton("tx")).transactionAware().build();

			return cacheManager;
		}

		@Bean
		public CacheManager cacheManager5() {
			RedisCacheWriter cacheWriter = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory());

			FormattingConversionService conversionService = new DefaultFormattingConversionService(true);
			conversionService.addFormatter(new DateFormatter("yyyy-MM-dd"));

			RedisCacheConfiguration lineConfig = RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues()
					.entryTtl(Duration.ofMinutes(2)).withConversionService(conversionService)
					.serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
					.serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

			RedisCacheConfiguration helloConfig = RedisCacheConfiguration.defaultCacheConfig()
					.entryTtl(Duration.ofSeconds(90));

			RedisCacheConfiguration worldConfig = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ZERO)
					.withConversionService(conversionService)
					.serializeValuesWith(SerializationPair.fromSerializer(new JdkSerializationRedisSerializer()));

			RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
					.entryTtl(Duration.ofMinutes(5))
					.serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

			// 配置指定cacheName对应的RedisCacheConfiguration对象
			Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
			cacheConfigs.put("line", lineConfig);
			cacheConfigs.put("hello", helloConfig);
			cacheConfigs.put("world", worldConfig);

			/*
			 * RedisCacheManagerBuilder的initialCacheNames方法的实现流程是：
			 * 
			 * 1.创建一个Map，对于每一个cacheName，都使用cacheDefaults来赋值到Map的value中。即：map.put
			 * (cacheName,defaultConfig)。
			 * 
			 * 2.使用intialCaches.putAll(map)的方式，
			 * 给RedisCacheManagerBuilder的intialCaches属性增加key和value
			 * 
			 * 从这里可以看到一个问题，
			 * 如果我先使用了RedisCacheManager的RedisCacheManagerBuilder的withInitialCacheConfigurations方法
			 * ，对intialCaches属性赋值了。那么再使用initialCacheNames方法时，
			 * intialCaches中的key就都会被赋值为defaultConfig，
			 * 这样withInitialCacheConfigurations方法给出的配置就无效了。
			 * 
			 * 总之，RedisCacheManagerBuilder的正确赋值顺序为：
			 * 
			 * 1.cacheDefaults（设置默认config）
			 * 
			 * 2.initialCacheNames（让所有的Cache都对应默认config）
			 * 
			 * 3.withInitialCacheConfigurations（对指定的Cache给出指定的config，
			 * 覆盖掉默认config）
			 */
			RedisCacheManager cacheManager = RedisCacheManager.builder(cacheWriter).cacheDefaults(defaultConfig)
					.initialCacheNames(Sets.newHashSet("hello", "line", "world", "test", "user"))
					.withInitialCacheConfigurations(cacheConfigs).build();

			return cacheManager;
		}

		@Bean
		public TaskExecutor threadPoolTaskExecutor() {
			ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
			taskExecutor.setCorePoolSize(10);
			taskExecutor.setMaxPoolSize(20);
			taskExecutor.setQueueCapacity(3);
			taskExecutor.setAllowCoreThreadTimeOut(false);
			taskExecutor.setKeepAliveSeconds(120);
			taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
			taskExecutor.setAwaitTerminationSeconds(3000);
			taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
			taskExecutor.setThreadGroupName("async-task-executor-group");
			taskExecutor.setThreadNamePrefix("async-task-executor-");

			return taskExecutor;
		}

		@Bean
		public TaskScheduler threadPoolTaskScheduler() {
			ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
			taskScheduler.setPoolSize(20);
			taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
			taskScheduler.setAwaitTerminationSeconds(30);
			taskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
			taskScheduler.setThreadNamePrefix("scheduled-task-executor-");

			return taskScheduler;
		}

		@Bean
		public CacheOperationLogger cacheOperationLogger() {
			return new CacheOperationLogger();
		}

		@Bean
		public DataSourceTransactionManager transactionManager(DataSource dataSource) {
			DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

			return transactionManager;
		}
	}

	@Autowired
	private CacheService service;

	@Test
	@Ignore
	public void test1() {
		String result1 = service.test1(12, 90);
		String result2 = service.test1(12, 90);

		/*
		 * 注意：如果使用RedisCache作为缓存处理的话，此时test1==test2会不成立，但第二次执行service.test1(12,
		 * 90) 确实是走缓存了。
		 * 
		 * 这是因为，相比EhCacheCache和ConcurrentMapCache是把key和value存储到内存中，
		 * RedisCache是把key和value存到redis数据库中，然后再从缓存中获取数据时，获取的是value的反序列化的结果，
		 * 因此自然每一次返回的对象都是不同的对象。但对象的“内容”都是相同的。
		 */
		assert result1 != result2;
		assert result1.equals(result2);

		String result3 = service.test1(19, 30);
		String result4 = service.test1(19, 30);

		assert result3.equals(result4);
	}

	@Test
	@Ignore
	public void test2() {
		Date date = new Date();
		// 实际调用目标对象的test2方法是在新线程中进行的
		ListenableFuture<Date> future1 = service.test2(date, 15);

		/*
		 * 如果执行此行时，service的test2方法还没有执行完，那么该addCallback方法立即执行完毕，等test2方法执行完毕后，
		 * 会在一个新的线程中执行System.out::println。
		 * 
		 * 如果执行此行时，service的test2方法执行完了，那么当前线程会继续调用System.out::println。
		 */
		future1.addCallback(System.out::println, Throwable::printStackTrace);
	}

	@Test
	@Ignore
	public void test3() throws ParseException {
		Date date = DateFormat.getDateInstance().parse("2010-09-21");
		Date offSetDate1 = service.test3(date, 10);
		Date offSetDate2 = service.test3(date, 10);

		assert offSetDate1 != offSetDate2;
		assert offSetDate1.equals(offSetDate2);
	}

	@SuppressWarnings("unused")
	@Test
	@Ignore
	public void test4() {
		BaseDept dept1 = service.test4(20);
		BaseDept dept2 = service.test4(20);
		BaseDept dept3 = service.test4(25);
		BaseDept dept4 = service.test4(25);
	}

	@SuppressWarnings("unused")
	@Test
	@Ignore
	public void test5() {
		BaseLine line1 = service.test5(609, 5);
		BaseLine line2 = service.test5(609, 5);
		BaseLine line3 = service.test5(911, 20);
		BaseLine line4 = service.test5(911, 20);
	}

	@SuppressWarnings("unused")
	@Test
	@Transactional
	@Commit
	@Ignore
	public void test6() {
		BaseLine line1 = service.test6(608, 1);
		BaseLine line2 = service.test6(608, 1);
		BaseLine line3 = service.test6(357, 5);
		BaseLine line4 = service.test6(357, 5);
		BaseLine line5 = service.test6(989, 20);
		BaseLine line6 = service.test6(989, 20);
	}

	@SuppressWarnings("unused")
	@Test
	public void test7() throws ParseException {
		Date date1 = new Date();
		Date date2 = DateFormat.getDateInstance().parse("2017-11-5");

		BaseLine line1 = service.test7(date1);
		BaseLine line2 = service.test7(date1);
		BaseLine line3 = service.test7(date2);
		BaseLine line4 = service.test7(date2);

		BaseDept dept1 = service.test8(15, "No:15", "15dept");
		BaseDept dept2 = service.test8(15, "No:15", "15dept");
	}

}
