package com.spring.data.redis.test;

import static com.spring.redis.data.domain.DomainUtils.generateDepts;
import static com.spring.redis.data.domain.DomainUtils.generateLine;
import static com.spring.redis.data.domain.DomainUtils.generateLines;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.spring.redis.data.domain.BaseLine;
import com.spring.redis.data.service.TestService;

@RunWith(SpringRunner.class)
@ContextHierarchy({ @ContextConfiguration("classpath:dataSource.xml"),
		@ContextConfiguration("classpath:spring-redis-transactional.xml") })
@TestPropertySource(properties = { "year=2018", "date1=${year}-11-2", "date2=${year}-10-27", "date3=${year}-5-31" })
public class TransactionalExcerciseTest {

	@Resource(name = "redisTemplate")
	private RedisTemplate<Date, Object> dateObjectRedisTemplate;

	@Value("${date1}")
	private Date date1;

	@Value("${date2}")
	private Date date2;

	@Value("${date3}")
	private Date date3;

	@Autowired
	private TestService service;

	@Test(expected = InvalidDataAccessApiUsageException.class)
	@Ignore
	public void test1() {
		dateObjectRedisTemplate.multi();

		ListOperations<Date, Object> opsForList = dateObjectRedisTemplate.opsForList();
		Long size1 = opsForList.leftPushAll(date1, generateDepts(30, 50, 99, 81, 25, 60));
		System.out.println(size1);

		opsForList.rightPushAll(date1, generateLines(608, 5, 609, 8, 877, 20, 909, 103));
		opsForList.rightPush(date1, "hello world");
		opsForList.rightPush(date1, 88776);

		// 由于没有事务支持，每一次RedisTemplate的操作，都会创建一次RedisConnection对象，然后在用完后就会关闭。
		Object objPoped1 = opsForList.leftPop(date1);
		Object objPoped2 = opsForList.rightPop(date1);

		// 因此，和redis的操作即时就被执行了，不会返回null。
		assert Objects.nonNull(objPoped1);
		assert Objects.nonNull(objPoped2);

		List<Object> listRange = opsForList.range(date1, -5, -2);
		System.out.println(listRange);

		/*
		 * 此时会报错，因为当前没有事务支持，那么直接执行exec方法，实际是创建一个新的RedisConnection对象并调用exec方法。
		 * 由于此前这个RedisConnection对象并没有被调用multi方法，因此直接执行exec方法自然会报错。
		 */
		dateObjectRedisTemplate.exec();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	@Ignore
	public void test2() {

		HashOperations<Date, String, Object> opsForHash = dateObjectRedisTemplate.opsForHash();
		Date date = new Date();

		HashMapper<Object, String, Object> hashMapper = new Jackson2HashMapper(false);
		BaseLine line = generateLine(50, 20);
		Map<String, Object> hash = hashMapper.toHash(line);

		// 由于此时RedisTemplate并没有开启事务支持，因此该方法直接进行和数据库的交互
		opsForHash.putAll(date, hash);

		List<Object> result = (List<Object>) dateObjectRedisTemplate
				.execute((SessionCallback) (RedisOperations redisOperations) -> {
					// 在SessionCallback中使用RedisOperations时，使用的都是同一个RedisConnection对象
					redisOperations.multi();
					BoundSetOperations setOps = redisOperations.boundSetOps(date2);
					setOps.add(generateDepts(6));
					setOps.add(generateLines(5));

					// Set distinctMembers = setOps.distinctRandomMembers(200);
					List members = setOps.randomMembers(100);
					// assert Objects.isNull(distinctMembers);
					assert Objects.isNull(members) || members.isEmpty();

					Long counts = setOps.add("hello", "hello", "zhangsan", 11, 28, "zhangsan", "world");
					assert Objects.isNull(counts);

					Object memberPoped1 = setOps.pop();
					Object memberPoped2 = setOps.pop();
					assert Objects.isNull(memberPoped1);
					assert Objects.isNull(memberPoped2);

					List<Object> txResult = redisOperations.exec();
					return txResult;
				});

		System.out.println(result);

		// 由于此时RedisTemplate并没有开启事务支持，因此该方法直接进行和数据库的交互
		Map<String, Object> hashFromRedis = opsForHash.entries(date);
		BaseLine lineFromHash = (BaseLine) hashMapper.fromHash(hashFromRedis);
		System.out.println(lineFromHash.getLineName() + ":" + lineFromHash.getDept().getDeptName());
	}

	@Test
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	public void test3() throws ParseException {
		service.service();
	}

}