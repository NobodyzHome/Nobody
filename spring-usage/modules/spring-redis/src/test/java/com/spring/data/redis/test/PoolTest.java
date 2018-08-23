package com.spring.data.redis.test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class PoolTest {

	@Configuration
	static class Config {

		@Bean
		public JedisConnectionFactory connFactory() {
			RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration("localhost",
					63790);
			redisStandaloneConfiguration.setDatabase(0);
			redisStandaloneConfiguration.setPassword(RedisPassword.of("m500420620"));

			GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
			poolConfig.setMinIdle(10);
			poolConfig.setMaxIdle(20);
			poolConfig.setMaxTotal(50);

			// 设置当池子里所有元素都被租用（borrow）后，再有客户端想租用池子里的对象时，这个客户端线程是否应被阻塞，以等待池子中有元素被归还(return)
			poolConfig.setBlockWhenExhausted(true);
			/*
			 * 如果blockWhenExhausted为true，那么当客户端想租用池子里的元素并且池子里没有元素可被租用时，
			 * 当前线程最长的被阻塞时间，超过这个时间，当前线程则会抛出异常，解除阻塞
			 */
			poolConfig.setMaxWaitMillis(TimeUnit.SECONDS.toMillis(5));

			// 设置一个对象在池中最长的空闲（idle）时间，过了这个时间后这个对象就有可能被池子清除了。
			// MinEvictableIdleTime的意思是池子里元素最小的可被清除的时间，过了这个时间，该元素就被可以被清除了。换个思路想也就是最大的池子里的元素的空闲时间
			poolConfig.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(5));

			JedisClientConfiguration clientConfig = JedisClientConfiguration.builder()
					.connectTimeout(Duration.ofSeconds(5)).readTimeout(Duration.ofSeconds(5)).usePooling()
					.poolConfig(poolConfig).build();

			JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration,
					clientConfig);

			return jedisConnectionFactory;
		}

		@Bean
		public StringRedisTemplate stringRedisTempldate(JedisConnectionFactory connFactory) {
			StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(connFactory);

			return stringRedisTemplate;
		}

	}

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Test
	@Ignore
	public void test1() {
		ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
		opsForValue.set("hello", "zhangsansan");
		opsForValue.append("hello", " world");

		System.out.println(opsForValue.get("hello"));

		stringRedisTemplate.execute((RedisConnection conn) -> {
			StringRedisConnection strConn = (StringRedisConnection) conn;
			strConn.multi();
			System.out.println(strConn.set("count", "0"));
			System.out.println(strConn.incrBy("count", 30));
			System.out.println(strConn.decrBy("count", -16));
			System.out.println(strConn.lPush("count", "test1", "test2"));
			System.out.println(strConn.getSet("count", "177"));
			System.out.println(strConn.sIsMember("count", "haha"));
			System.out.println(strConn.expire("count", 90));
			System.out.println(strConn.ttl("count"));
			strConn.exec();

			return null;
		});
		
	}

}