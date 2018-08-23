package com.spring.redis.data.service;

import static com.spring.redis.data.domain.DomainUtils.generateDept;
import static com.spring.redis.data.domain.DomainUtils.generateDepts;
import static com.spring.redis.data.domain.DomainUtils.generateLine;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

public class TestService {

	@Autowired
	private RedisTemplate<Date, Object> redisTemplate;

	public void service() throws ParseException {
		redisTemplate.setEnableTransactionSupport(true);

		ValueOperations<Date, Object> opsForValue = redisTemplate.opsForValue();
		SetOperations<Date, Object> opsForSet = redisTemplate.opsForSet();

		Date date1 = DateFormat.getDateInstance().parse("2017-11-20");
		Date date2 = DateFormat.getDateInstance().parse("2018-5-6");

		// 对于非只读操作，进入到事务队列
		opsForValue.set(date1, generateLine(611, 5));
		// 对于非只读操作，进入到事务队列
		Object val1 = opsForValue.getAndSet(date1, generateDept(12));
		assert val1 == null;

		// 对于只读操作，立即进行查询
		Long size = opsForValue.size(date1);

		// 对于只读操作，立即进行查询
		Date dept = redisTemplate.randomKey();
		System.out.println(dept);

		// 以下三行和redis的操作会被加入到事务队列中
		opsForSet.add(date2, generateDepts(5));
		opsForSet.add(date2, generateDepts(6));
		opsForSet.add(date2, "zhang san", "li si", "zhang san", 98, 98, 102);

		// 对于只读操作，立即进行查询
		Set<Object> distinctMemebers = opsForSet.distinctRandomMembers(date2, 200);
		System.out.println(distinctMemebers);
		// @Transactional方法执行完毕后，会自动进行当前RedisConnection的exec或discard操作
	}
}
