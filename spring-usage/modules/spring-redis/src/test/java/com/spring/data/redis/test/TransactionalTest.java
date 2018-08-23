package com.spring.data.redis.test;

import static com.spring.redis.data.domain.DomainUtils.generateDept;
import static com.spring.redis.data.domain.DomainUtils.generateLine;
import static com.spring.redis.data.domain.DomainUtils.generateLines;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.spring.redis.aop.aspect.RedisConnectionLogger;
import com.spring.redis.data.domain.BaseDept;
import com.spring.redis.data.domain.BaseLine;

@RunWith(SpringRunner.class)
@ContextConfiguration
@DirtiesContext(hierarchyMode = HierarchyMode.CURRENT_LEVEL, classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class TransactionalTest {

	@Configuration
	@EnableAspectJAutoProxy
	@EnableTransactionManagement
	@ImportResource("classpath:dataSource.xml")
	static class Config {

		@Bean
		public RedisConnectionFactory connectionFactory() {
			RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration("localhost", 63790);
			serverConfig.setDatabase(0);

			GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
			poolConfig.setMinIdle(5);
			poolConfig.setMaxIdle(3);
			poolConfig.setMaxTotal(10);
			poolConfig.setMaxWaitMillis(TimeUnit.SECONDS.toMillis(90));
			poolConfig.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(3));

			JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().clientName("maziqiang")
					.connectTimeout(Duration.ofMinutes(2)).readTimeout(Duration.ofSeconds(30)).usePooling()
					.poolConfig(poolConfig).build();

			return new JedisConnectionFactory(serverConfig, clientConfig);
		}

		@Bean
		public StringRedisSerializer stringRedisSerializer() {
			return new StringRedisSerializer(Charset.defaultCharset());
		}

		@Bean
		public ConversionService conversionService() {
			FormattingConversionService conversionService = new FormattingConversionService();
			conversionService.addFormatter(new DateFormatter("yyyy-MM-dd"));

			return conversionService;
		}

		@Bean
		public GenericToStringSerializer<Date> dateRedisSerializer() {
			GenericToStringSerializer<Date> dateRedisSerializer = new GenericToStringSerializer<Date>(Date.class);
			dateRedisSerializer.setConversionService(conversionService());

			return dateRedisSerializer;
		}

		@Bean
		public JdkSerializationRedisSerializer jdkRedisSerializer() {
			return new JdkSerializationRedisSerializer();
		}

		@Bean
		public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
			return new GenericJackson2JsonRedisSerializer();
		}

		@Bean
		public Jackson2JsonRedisSerializer<BaseDept> jackson2JsonRedisSerializer() {
			return new Jackson2JsonRedisSerializer<>(BaseDept.class);
		}

		@Bean
		public RedisTemplate<BaseDept, Object> deptObjTemplate() {
			RedisTemplate<BaseDept, Object> deptObjTemplate = new RedisTemplate<>();
			deptObjTemplate.setConnectionFactory(connectionFactory());
			deptObjTemplate.setKeySerializer(jackson2JsonRedisSerializer());
			deptObjTemplate.setValueSerializer(jdkRedisSerializer());
			deptObjTemplate.setHashKeySerializer(stringRedisSerializer());
			deptObjTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer());

			return deptObjTemplate;
		}

		@Bean
		public RedisConnectionLogger logger() {
			return new RedisConnectionLogger();
		}

		@Bean
		public Service service() {
			return new Service(deptObjTemplate());
		}

		@Bean
		public ThreadPoolTaskExecutor taskExecutor() {
			ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
			taskExecutor.setCorePoolSize(5);
			taskExecutor.setMaxPoolSize(20);
			taskExecutor.setQueueCapacity(3);
			taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
			taskExecutor.setAllowCoreThreadTimeOut(false);
			taskExecutor.setKeepAliveSeconds(60);
			taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
			taskExecutor.setAwaitTerminationSeconds(300);
			taskExecutor.setThreadGroupName("task-group");
			taskExecutor.setThreadNamePrefix("redis-executor-");

			return taskExecutor;
		}
	}

	@Autowired
	private RedisTemplate<BaseDept, Object> deptObjTemplate;

	@Autowired
	private Service service;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	public static class Service {

		private RedisTemplate<BaseDept, Object> redisTemplate;

		public Service(RedisTemplate<BaseDept, Object> redisTemplate) {
			this.redisTemplate = redisTemplate;
		}

		@Transactional
		public void service1() {
			HashOperations<BaseDept, String, Object> opsForHash = redisTemplate.opsForHash();
			ZSetOperations<BaseDept, Object> opsForZSet = redisTemplate.opsForZSet();
			ListOperations<BaseDept, Object> opsForList = redisTemplate.opsForList();

			BaseLine line = generateLine(33, 57);
			BaseDept dept = generateDept(99);

			HashMapper<Object, String, Object> jacksonHashMapper = new Jackson2HashMapper(false);
			Map<String, Object> hash = jacksonHashMapper.toHash(line);

			/*
			 * 如果RedisTemplate的enableTransactionSupport属性为false，那么在调用putAll方法时，
			 * 会获取一个RedisConnection对象，在putAll方法结束后，调用这个RedisConnection的close方法。
			 */
			opsForHash.putAll(dept, hash);
			Map<String, Object> hashFromRedis = opsForHash.entries(dept);

			if (!hashFromRedis.isEmpty()) {
				BaseLine lineFromRedis = (BaseLine) jacksonHashMapper.fromHash(hashFromRedis);
				System.out.println(lineFromRedis.getLineName() + ":" + lineFromRedis.getDept().getDeptName());
			}

			BaseDept dept1 = generateDept(77);
			opsForZSet.add(dept1, new Date(), 3.299);
			opsForZSet.add(dept1, generateLines(5), 1.778);
			opsForZSet.add(dept1, "test", -12.515);

			Set<Object> set1 = opsForZSet.rangeByScore(dept1, 1, 4.599);
			set1.forEach(obj -> System.out.println(obj.getClass() + "   " + obj));

			redisTemplate.hasKey(dept1);
		}
	}

	@Test
	@Ignore
	public void test9() {
		service.service1();
	}

	/*
	 * 一、无事务支持：无需对RedisTemplate进行配置
	 * 
	 * 我们来看每一次调用RedisTemplate的和redis交互的操作(例如hasKey方法)，其实际执行流程如下:
	 * 
	 * 1.创建RedisConnection对象（从池子里借出redis连接或直接创建物理连接）
	 * 
	 * 2.使用RedisConnection进行和redis的交互操作
	 * 
	 * 3.调用这个RedisConnection对象的close方法（向池子归还redis连接或直接关闭物理连接）
	 */
	@Test
	@Ignore
	public void test1() {

		ListenableFuture<Boolean> listenableResult = taskExecutor
				.submitListenable(() -> deptObjTemplate.hasKey(generateDept()));
		listenableResult.addCallback(System.out::println, Throwable::printStackTrace);

		HashOperations<BaseDept, String, Object> opsForHash = deptObjTemplate.opsForHash();
		ZSetOperations<BaseDept, Object> opsForZSet = deptObjTemplate.opsForZSet();

		BaseLine line = generateLine(33, 57);
		BaseDept dept = generateDept(99);

		HashMapper<Object, String, Object> jacksonHashMapper = new Jackson2HashMapper(false);
		Map<String, Object> hash = jacksonHashMapper.toHash(line);

		opsForHash.putAll(dept, hash);

		Map<String, Object> hashFromRedis = opsForHash.entries(dept);

		BaseLine lineFromRedis = (BaseLine) jacksonHashMapper.fromHash(hashFromRedis);
		System.out.println(lineFromRedis.getLineName() + ":" + lineFromRedis.getDept().getDeptName());

		BaseDept dept1 = generateDept(77);

		taskExecutor.execute(() -> opsForZSet.add(dept1, new Date(), 3.299));
		taskExecutor.execute(() -> opsForZSet.add(dept1, generateLines(5), 1.778));
		taskExecutor.execute(() -> opsForZSet.add(dept1, "test", -12.515));

		ListenableFuture<Set<Object>> listenableFuture = taskExecutor
				.submitListenable(() -> opsForZSet.rangeByScore(dept1, 1, 4.599));
		listenableFuture.addCallback(set -> set.forEach(obj -> System.out.println(obj.getClass() + " " + obj)),
				Throwable::printStackTrace);

		deptObjTemplate.hasKey(dept1);
	}

	@Test
	@Ignore
	public void test2() {
		deptObjTemplate.setEnableTransactionSupport(false);

		BoundHashOperations<BaseDept, String, Object> boundHashOps = deptObjTemplate.boundHashOps(generateDept(77));
		deptObjTemplate.multi();

		Jackson2HashMapper hashMapper = new Jackson2HashMapper(false);

		BaseLine line = generateLine(90, 123);
		Map<String, Object> lineHash = hashMapper.toHash(line);

		boundHashOps.putAll(lineHash);
		boundHashOps.put("test", Calendar.getInstance());
		boundHashOps.increment("lineNo", 3.991387);

		/*
		 * 执行exec方法时会报错：org.springframework.dao.
		 * InvalidDataAccessApiUsageException: No ongoing transaction. Did you
		 * forget to call multi?
		 * 
		 * 异常中提示我们是否调用了multi方法，但我们在前面已经调用过multi方法了，为什么还会报这个错误？我们来研究下这其中的原因：
		 * 由于RedisTemplate的enableTransactionSupport属性为false，
		 * 因此每次调用RedisTemplate方法，
		 * 都是使用RedisConnectionFactory来重新创建一个RedisConnection对象。
		 * 
		 * 因此，在上面的deptObjTemplate.multi()方法时，就是新创建了一个RedisConnection对象，
		 * 并调用其multi方法。
		 * 
		 * 而执行到deptObjTemplate.exec()方法时，也是新创建了一个RedisConnection对象，并调用其exec方法。
		 * 由于这个新创建的RedisConnection对象和调用deptObjTemplate.multi()
		 * 方法时创建的RedisConnection对象不是同一个对象。因此，当前这个RedisConnection对象并没有被调用过multi方法
		 * 。因此，spring才会认为执行deptObjTemplate.exec()时创建的RedisConnection对象没有开启事务，
		 * 报出异常。
		 */
		List<Object> commitResult = deptObjTemplate.exec();
		System.out.println(commitResult);
	}

	/*
	 * 二、使用RedisTemplate的execute(SessionCallback)方法进行事务支持。无需对RedisTemplate进行任何配置
	 * 
	 * 之所以test2()中会出现问题，归根结底是每次调用RedisTemplate的和redis交互的方法，
	 * 获得的都是新创建的RedisConnection对象。但实际期望的是使用同一个RedisConnection对象。
	 * 
	 * 为了解决test2()方法中的问题，可以使用RedisTemplate.execute(SessionCallback)方法来解决这个问题。
	 * 以下是RedisTemplate.execute方法的执行流程：
	 * 
	 * 1.创建RedisConnection对象（从池子里借出redis连接或直接创建物理连接）
	 * 
	 * 2.将这个RedisConnection对象绑定到当前线程上
	 * 
	 * 3.在SessionCallback的execute方法中，所有使用RedisOperations（其实际也就是使用RedisTemplate）
	 * 进行和redis交互的操作，都是使用这个和当前线程绑定的RedisConnection对象
	 * 
	 * 4.在SessionCallback的execute方法执行完毕后，解绑当前线程上的RedisConnection对象，
	 * 
	 * 5.调用这个RedisConnection对象的close方法（向池子归还redis连接或直接关闭物理连接）
	 * 
	 * 因此，在SessionCallback的execute方法中，所有使用的和redis的交互都是使用的同一个RedisConnection对象。
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	@Ignore
	public void test3() {
		deptObjTemplate.setEnableTransactionSupport(false);

		/*
		 * deptObjTemplate由于没有开启事务支持，因此下行代码的执行流程是：
		 * 
		 * 1.创建RedisConnection对象
		 * 
		 * 2.使用RedisConnection对象执行hasKey方法
		 * 
		 * 3.关闭RedisConnection对象
		 */
		deptObjTemplate.hasKey(generateDept());

		/*
		 * 在执行SessionCallback前，RedisTemplate的方法会：
		 * 
		 * 1.创建一个RedisConnection对象
		 * 
		 * 2.将这个RedisConnection对象绑定到当前线程
		 */
		List<Object> result = (List<Object>) deptObjTemplate.execute((SessionCallback) (RedisOperations operations) -> {

			BaseDept dept = generateDept(99);
			BaseLine line1 = generateLine(103, dept.getDeptNo());
			BaseLine line2 = generateLine(608, dept.getDeptNo());
			BaseLine line3 = generateLine(299, dept.getDeptNo());

			// 在execute方法里，所有对redis的操作都是使用同一个RedisConnection对象，即当前线程绑定的RedisConnection对象
			/*
			 * 在SessionCallback中使用RedisOperations，下行代码的执行流程是：
			 * 1.获取当前线程绑定的RedisConnection对象
			 * 
			 * 2.使用RedisConnection对象执行multi方法
			 */
			operations.multi();
			ListOperations<BaseDept, Object> opsForList = operations.opsForList();

			opsForList.rightPushAll(dept, line1, line2, line3, new Date(), Calendar.getInstance());
			opsForList.leftPushAll(dept, "xiaoming", "zhangsan", 850.3231);

			// 由于当前连接处于事务状态中，因此在redis中不会马上执行leftPop和rightPop命令，因此这些方法返回的固定是null
			Object obj1 = opsForList.leftPop(dept);
			Object obj2 = opsForList.rightPop(dept);

			assert Objects.isNull(obj1);
			assert Objects.isNull(obj2);

			// 注意：在exec返回的List中，也是使用RedisTemplate的RedisSerializer对每一个命令的返回结果进行反序列化。
			List<Object> execRst = operations.exec();

			return execRst;

			/*
			 * 在SessionCallback执行完成后，RedisTemplate的execute方法会：
			 * 
			 * 1.获取和当前线程绑定的RedisConnection对象
			 * 
			 * 2.调用RedisConnection对象的close方法
			 * 
			 * 3.取消这个RedisConnection对象对象和当前线程的绑定
			 */
		});

		System.out.println(result);
	}

	/*
	 * 三、使用spring-redis的@Transactional事务支持，需要对RedisTemplate进行配置
	 * 
	 * 好，现在我们已经有解决test2方法的问题了，但依然有改进的地方：即事务的自动开启和结束。
	 * 能不能像jdbc一样，在Dao层就只进行和redis数据库，不涉及事务提交或回滚。而在进入Service层方法前就创建redis连接，
	 * 并将其绑定到线程上，在Service层方法执行后，就自动exec或discard事务，然后关闭redis数据库。
	 * 
	 * 这就是更高级的spring-redis对事务的支持：即@Transactional支持。spring-redis的@
	 * Transactional支持和普通的@Transactional使用一样，需要PlatformTransactionManager。但很奇怪的是
	 * ，spring-redis并没有提供相关于redis自己的PlatformTransactionManager。
	 * 于是我就创建了一个内嵌数据库hsql的DataSource，然后创建了DataSourceTransactionalManager。
	 * 之后，就可以在Bean的方法或@Test的方法上增加@Transactional注解。
	 * 
	 * 开启spring-redis的@Transactional事务支持所需要的配置
	 * 
	 * 1.将要使用的RedisTemplate的enableTransactionSupport属性设置为true
	 * 
	 * 2.在容器中配置PlatformTransactionManager的<bean>，可以是任意实现类，例如：
	 * DataSourceTransactionManager。
	 * 
	 * 3.在需要自动执行事务的<bean>的方法上增加@Transactional注解
	 * 
	 * 4.在容器配置中增加@EnableTransactionManagement注解，使容器能识别出<bean>上的@Transational注解，
	 * 并对这个<bean>进行代理
	 * 
	 * 当开启了spring-redis的@Transactional支持后，RedisTemplate会为RedisConnection对象创建代理。
	 * 当调用非只读方法（例如:set(),zadd()）时，会使用和当前线程绑定的RedisConnection对象执行。而调用read-only方法（
	 * 例如:keys(),get(),lrange()）时，会获取一个新的RedisConnection对象，然后执行方法。
	 * 其目的是因为和当前线程绑定的RedisConnection对象已经被调用multi方法开启了事务模式，所以使用该对象进行查询时，只能返回null。
	 * 因此，对于查询的命令，使用一个新的RedisConnection对象来执行，这样，就可以直接查询出数据了。
	 * 
	 * 开启@Transactional事务支持后，使用RedisTemplate进行操作的流程是：
	 * 
	 * 1.创建RedisConnection对象，然后创建这个RedisConnection对象的代理对象。该代理对象对于redis的所有read-
	 * only操作，都获取一个新的RedisConnection对象来执行。
	 * 
	 * 2.如果不是@Transactional(read-only=true)，则调用RedisConnection对象的multi方法，开启事务处理
	 * 
	 * 3.注册一个事务任务RedisTransactionSynchronizer，
	 * 该事务任务执行的内容是在PlatformTransactionManager执行完毕后，
	 * 调用RedisConnection的exec或discard方法，然后调用close方法
	 * 
	 * 4.将该RedisConnection对象和当前线程绑定
	 * 
	 * 5.当前线程后续使用RedisTemplate和redis交互的操作，都是使用这个RedisConnection对象
	 * 
	 * 6.当前线程@Transactional方法执行完毕后，调用RedisTransactionSynchronizer，进行事务和连接关闭操作，
	 * 然后将RedisConnection对象和当前线程解绑
	 * 
	 * 注意：在jdbc的@Transactional处理中，是在@Transactional方法执行之前就开启数据库连接和事务，
	 * 在方法执行之后就提交或回滚事务，然后关闭数据库连接。而spring-redis中的处理则稍微有一点区别，在执行@Transactional方法之前
	 * ，并不开启redis连接和事务，而是在@Transactional方法中第一次使用RedisTemplate和redis交互的方法时，
	 * 才会开启redis连接和事务。
	 */
	@Test
	@Transactional
	// 从该方法中可以看到，该方法没有编写任何事务处理的操作，但该方法使用deptObjTemplate时，实际是进行了事务开启和提交操作的。
	public void test4() {
		// spring-redis的@Transactional支持必须把RedisTemplate的enableTransactionSupport属性设置为true
		deptObjTemplate.setEnableTransactionSupport(true);

		HashOperations<BaseDept, String, Object> opsForHash = deptObjTemplate.opsForHash();

		HashMapper<Object, String, Object> jacksonHashMapper = new Jackson2HashMapper(false);
		BaseLine line = generateLine();
		Map<String, Object> lineHash = jacksonHashMapper.toHash(line);

		BaseDept dept = generateDept();
		// 第一次使用RedisTemplate进行和redis的交互操作，此时会：
		// a) 创建RedisConnection对象
		// b)
		// 创建这个RedisConnection对象的代理。该代理对于所有read-only操作，都会重新获取一个新的RedisConnection对象来执行
		// c) 调用RedisConnection对象的multi方法，进入事务模式
		// d) 将这个RedisConnection对象绑定到当前线程
		// e)
		// 注册事务任务RedisTransactionSynchronization，用于在@Transactional方法结束后进行exec/discard操作和连接关闭操作
		// f) 使用这个RedisConnection对象执行putAll操作
		// 注意：使用@Transactional事务，无需手动执行multi方法来开启事务
		opsForHash.putAll(dept, lineHash);

		// 第二次调用和redis的交互操作，此时会：
		// a) 获取和当前线程绑定的RedisConnection对象
		// b) 使用这个RedisConnection对象执行put操作
		// 注意：此时的操作和putAll操作使用的是同一个RedisConnection对象中，也就是在同一个事务里
		opsForHash.put(dept, "test", new Date());

		BaseDept dept1 = generateDept(99);

		SetOperations<BaseDept, Object> opsForSet = deptObjTemplate.opsForSet();
		// 当前线程的后续使用RedisTemplate进行的和redis的非read-only操作，其执行流程都和执行opsForHash.put的流程是一样的
		opsForSet.add(dept1, Calendar.getInstance());
		opsForSet.add(dept1, (Object[]) generateLines(3));
		assert Objects.isNull(opsForSet.pop(dept1));

		/*
		 * 好的，不一样的来了。这个操作和上面的操作的执行流程是不一样的，因为它是read-only操作。
		 * RedisConnection的代理对象对read-only的命令的执行步骤如下：
		 * 
		 * a) 获取和当前线程绑定的RedisConnection对象
		 * 
		 * b) 代理的RedisConnection对象在执行read-only方法时，会重新获取一个RedisConnection对象
		 * 
		 * c) 使用这个新获取的RedisConnection对象进行read-only操作
		 * 
		 * d) 执行完毕后，关闭新获取的RedisConnection对象
		 * 
		 * 因此，在执行entries方法时，是立即就进行查询操作的，而没有进入到事务的操作队列中。
		 */
		opsForHash.entries(dept);

		// 由于该操作也是read-only操作，因此其执行流程和entries方法一样。也是获取一个新的RedisConnection对象，然后执行查询。也是立即执行查询操作。
		deptObjTemplate.hasKey(dept);

		/*
		 * 当前@Transactional方法执行完毕后，会进行DataSource中Connection的事务提交或回滚操作，
		 * 然后触发事务任务RedisTransactionSynchronization：
		 * 
		 * 1.根据@Transactional方法的执行情况，决定调用RedisConnection的exec或discard方法。
		 * 
		 * 2.调用当前线程绑定的RedisConnection对象的close方法
		 * 
		 * 3.解绑当前线程所绑定的RedisConnection对象
		 */

		/*
		 * 可以看到，在spring-redis的@Transaction事务流程中，@
		 * Transactional方法中第一次执行RedisTemplate和redis交互的地方时，会创建连接，开启事务。在@
		 * Transactional方法结束后，会exec或discard事务
		 */
	}
}