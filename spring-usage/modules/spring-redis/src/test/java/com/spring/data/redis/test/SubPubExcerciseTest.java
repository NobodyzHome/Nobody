package com.spring.data.redis.test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.spring.redis.data.domain.BaseDept;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-redis-subpub-excercise.xml")
public class SubPubExcerciseTest {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private StringRedisSerializer stringRedisSerializer;

	@Autowired
	private JdkSerializationRedisSerializer serializationRedisSerializer;

	@Autowired
	private Jackson2JsonRedisSerializer<BaseDept> jacksonRedisSerializer;

	@Autowired
	private GenericToStringSerializer<Date> genericToStringSerializer;

	@Autowired
	private GenericJackson2JsonRedisSerializer genericJacksonRedisSerializer;

	@Test
	public void test1() {
		stringRedisTemplate.convertAndSend("chatroom", "hello every body");
		stringRedisTemplate.convertAndSend("hello", "a good day");
		stringRedisTemplate.convertAndSend("test", "2017-10-21 10:30:55");

		stringRedisTemplate.execute((RedisConnection conn) -> {
			BaseDept dept = new BaseDept(13, "No.11", "13公司");
			dept.setModifyDate(new Date());

			byte[] deptMessage = jacksonRedisSerializer.serialize(dept);
			conn.publish(stringRedisSerializer.serialize("dept"), deptMessage);

			byte[] message1 = serializationRedisSerializer.serialize(dept);
			conn.publish(stringRedisSerializer.serialize("test"), message1);

			byte[] message2 = genericToStringSerializer.serialize(new Date());
			conn.publish(stringRedisSerializer.serialize("user"), message2);

			conn.publish(stringRedisSerializer.serialize("test"), message2);

			byte[] genericDeptMessage = genericJacksonRedisSerializer.serialize(dept);
			/*
			 * 在redis中，genericDept这个key的value是{"@class":
			 * "com.spring.redis.data.domain.BaseDept","dept_no":13,"dept_code":
			 * "No.11","dept_name":"11Dept"}，由于value值中有@class属性，
			 * 因此这个value中记录了对象的类型。当使用GenericJackson2JsonRedisSerializer进行反序列化时，
			 * 无需给出要反序列化的目的类型，就可以转换成正确类型的对象。
			 * 
			 * 这也就是GenericJackson2JsonRedisSerializer类中没有设置目标类型的原因，
			 * 因为它记录到redis的value中就已经记录了对象的类型了。
			 * 
			 * 通常情况下： 一个RedisSerializer把对象记录到redis的value后，如果这个value中记录了这个对象的类型，
			 * 那么就不需要对这个RedisSerializer给出目标类型。就好像，
			 * JdkSerializationRedisSerializer和GenericJackson2JsonRedisSerializer
			 * ，它们记录到redis的value中都包含目标类型，所以这两个类都不用设置目标类型，
			 * 最简单的办法就是这两个类都没有设置目标类型的范型。
			 */
			conn.set(stringRedisSerializer.serialize("genericDept"), genericDeptMessage);

			/*
			 * 在redis中，baseDept这个key的value是{"dept_no":13,"dept_code":"No.11",
			 * "dept_name":"11Dept"}，
			 * 可以看到Jackson2JsonRedisSerializer并没有像GenericJackson2JsonRedisSerializer那样记录这个对象的
			 * “@Class”值，因此Jackson2JsonRedisSerializer需要给出目的类型，
			 * 好让它在反序列化baseDept这个key的值时，知道要把这个value转换成什么类型的对象。
			 * 
			 * 这也就是为什么GenericJackson2JsonRedisSerializer类不需要设置目标类型的范型类型，
			 * 而Jackson2JsonRedisSerializer需要设置目标类型的范型类型。
			 * 就是因为Jackson2JsonRedisSerializer记录到redis的value中没有记录要转换成对象的类型，
			 * 但GenericJackson2JsonRedisSerializer记录到redis的value中有。
			 */
			conn.set("baseDept".getBytes(), deptMessage);

			conn.publish("amazing".getBytes(), deptMessage);
			conn.publish("hi".getBytes(), deptMessage);

			byte[] message3 = genericJacksonRedisSerializer.serialize(new Date());
			conn.publish("hello".getBytes(), message3);
			conn.set("time".getBytes(), message3);

			return null;
		});

		try {
			Thread.sleep(TimeUnit.MINUTES.toMillis(2));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
