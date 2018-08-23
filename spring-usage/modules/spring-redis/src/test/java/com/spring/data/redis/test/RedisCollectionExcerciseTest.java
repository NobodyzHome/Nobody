package com.spring.data.redis.test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.data.redis.support.collections.RedisProperties;
import org.springframework.data.redis.support.collections.RedisSet;
import org.springframework.data.redis.support.collections.RedisZSet;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.spring.redis.data.domain.BaseLine;
import com.spring.redis.data.domain.DomainUtils;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-redis-collection.xml")
public class RedisCollectionExcerciseTest {

	@Resource
	private DefaultRedisList<Object> redisList;

	@Resource
	private RedisSet<Object> redisSet;

	@Resource
	private RedisZSet<Object> redisZSet;

	@Resource
	private RedisMap<String, Object> redisMap;

	@Resource
	private RedisProperties redisProperties;

	@Test
	public void test1() {
		redisList.add("user");
		redisList.addFirst(DomainUtils.generateDept());
		redisList.add(DomainUtils.generateLine());
		redisList.addFirst(18);
		redisList.addLast(new Date());

		Object obj = redisList.get(2);
		assert obj instanceof String;

		redisList.set(1, null);
		redisList.addLast(null);

		List<Object> result = redisList.range(0, -1);
		System.out.println(result);
	}

	@Test
	public void test2() {
		redisList.add("user");
		redisList.add(18);
		redisList.offerFirst(Calendar.getInstance());

		Object peek = redisList.peek();
		System.out.println(peek);

		redisSet.addAll(redisList);
		int setSize = redisSet.size();
		System.out.println(setSize);

		redisList.clear();
		redisList.addAll(redisSet);
	}

	@Test
	public void test3() {
		redisZSet.addAll(redisSet);
		redisZSet.add(390, -20.1);
		redisZSet.add("haha", -1);

		Set<Object> elements = redisZSet.rangeByScore(-5, 10);
		elements.forEach(System.out::println);

		// 添加已有的元素，那么只会替换该元素的score值
		redisZSet.add(390, -87);
		System.out.println(redisZSet.score(390));

		Set<Object> elements1 = redisZSet.removeByScore(-100, 0).range(0, -1);
		elements1.forEach(e -> System.out.println(e + ":" + redisZSet.score(e)));
	}

	@SuppressWarnings("rawtypes")
	@Test
	@Ignore
	public void test4() {
		Jackson2HashMapper hashMapper = new Jackson2HashMapper(false);
		Map<String, Object> hash = hashMapper.toHash(DomainUtils.generateLine(609, 5));

		redisMap.putAll(hash);
		Map dept = (Map) redisMap.get("dept");
		System.out.println(dept);

		Object oldLineNo = redisMap.put("lineNo", 911);
		System.out.println(oldLineNo);
		System.out.println(redisMap.get("lineNo"));

		BaseLine line = (BaseLine) hashMapper.fromHash(redisMap);
		System.out.println(line.getLineName() + ":" + line.getDept().getDeptName());
	}

	@Test
	public void test5() {
		redisProperties.setProperty("ZS", "zhangsan");
		redisProperties.setProperty("LS", "lisi");
		redisProperties.setProperty("user", "maziqiang");
		redisProperties.setProperty("hello", "world");

		String zhangsan = (String) redisProperties.get("ZS");
		System.out.println(zhangsan);

		redisProperties.setProperty("hello", "test");
	}
}
