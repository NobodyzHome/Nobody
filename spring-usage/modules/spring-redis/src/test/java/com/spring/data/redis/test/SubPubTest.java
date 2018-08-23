package com.spring.data.redis.test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.hash.DecoratingStringHashMapper;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.spring.redis.data.domain.BaseDept;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-redis-subpub.xml")
public class SubPubTest {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private GenericJackson2JsonRedisSerializer jacksonRedisSerializer;

	@Autowired
	private StringRedisSerializer stringRedisSerializer;

	@Autowired
	private JdkSerializationRedisSerializer serializationRedisSerializer;

	@Test
	@Ignore
	public void test1() {
		stringRedisTemplate.execute((RedisConnection conn) -> {
			BaseDept dept = new BaseDept(30, "No.30", "30Dept");
			dept.setModifyDate(new Date());

			byte[] message = jacksonRedisSerializer.serialize(dept);
			byte[] channel = stringRedisSerializer.serialize("hello");

			conn.publish(channel, message);

			Date date = new Date();
			byte[] message1 = serializationRedisSerializer.serialize(date);
			conn.publish(stringRedisSerializer.serialize("chatroom"), message1);

			return null;
		});

		stringRedisTemplate.convertAndSend("dept", "hi,every body");
	}

	@Test
	public void test2() {
		Jackson2HashMapper hashMapper = new Jackson2HashMapper(Jackson2ObjectMapperBuilder.json().build(), true);
		DecoratingStringHashMapper<Object> mapper = new DecoratingStringHashMapper<>(hashMapper);

		BaseDept dept = new BaseDept(11, "No.30", "30Dept");
		dept.setModifyDate(new Date());
		dept.setIsRun(false);

		Map<String, String> hash = mapper.toHash(dept);
		stringRedisTemplate.opsForValue().set("user", "maziqiang");

		BoundHashOperations<String, String, String> boundHashOps = stringRedisTemplate
				.<String, String>boundHashOps("dept");
		boundHashOps.putAll(hash);
	}
}
