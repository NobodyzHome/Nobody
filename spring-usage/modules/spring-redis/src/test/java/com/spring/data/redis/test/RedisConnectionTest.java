package com.spring.data.redis.test;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.spring.redis.data.domain.BaseDept;

public class RedisConnectionTest {

	public static void main(String[] args) {

		// 一、创建一个单独的redis服务器的连接配置
		RedisStandaloneConfiguration redisServerConfig = new RedisStandaloneConfiguration("localhost", 63790);
		redisServerConfig.setDatabase(0);
		redisServerConfig.setPassword(RedisPassword.none());

		/*
		 * 二、创建连接池配置对象GenericObjectPoolConfig，JedisClientConfiguration使用apache-
		 * pool构件中的GenericObjectPool类来作为redis的连接池
		 */
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		// 池子中尽力保证有空闲的元素的个数。例如设置为5.就是尽力保证有5个空闲的元素。
		poolConfig.setMinIdle(5);

		/*
		 * 池子中允许的最大的空闲元素的个数。例如设置为8，当池子中已经有8个空闲的元素时，有一个客户端再归还给池子一个元素后，
		 * 这个元素就不会进入到池子中，而是直接销毁。避免池子中有过多空闲的元素，造成资源的浪费。
		 */
		poolConfig.setMaxIdle(8);

		// 池子中所有元素的个数的总和（即空闲元素和被借出的元素的个数的总和）的最大值
		poolConfig.setMaxTotal(20);

		/*
		 * 当池子中没有空闲元素后，客户再向池子请求元素时，客户线程则会被阻塞，等待其他客户把元素归还给池子。
		 * 下面这个属性就是用于控制客户端向池子租借元素时（即调用池子的borrowObject()方法时），客户端线程被阻塞的最长时间。超过这个时间
		 * ，客户端线程则会收到一个异常，解除阻塞，也就代表着客户端不等待池子里有元素了。
		 * 
		 * 如果该属性值小于0，那么客户端调用池子的borrowObject()方法时，如果池子中没有空闲元素，客户端就会一直被阻塞，
		 * 直到池子中有空闲元素。
		 */
		poolConfig.setMaxWaitMillis(TimeUnit.SECONDS.toMillis(90));

		/*
		 * 池子中空闲（idle）元素的最小的可被清除的时间。空闲元素的存活时间在这个时间之前时，那么这个空闲元素稳定地不会被清除。
		 * 但是当空闲时间超过这个时间，那么这个空闲元素就变为可被清除的了。但这不意味着这个元素马上就会被清除，而是说这个元素就有可能被清除了。
		 * 在这个时间之前，这个空闲元素是肯定不会被清除的。
		 * 
		 * 例如：该属性设置值为60秒，一个空闲元素在池子中存在60秒之内时，这个元素肯定不会被清除。但一旦这个空闲元素的存活时间超过了60秒，
		 * 那么这个元素就变为可以被清除的了。
		 */
		poolConfig.setMinEvictableIdleTimeMillis(TimeUnit.SECONDS.toMillis(60));

		// 三、创建Redis客户端的配置：包括连接超时、连接池等配置
		JedisClientConfiguration redisClientConfig = JedisClientConfiguration.builder().clientName("maziqiang")
				.connectTimeout(Duration.ofSeconds(2)).readTimeout(Duration.ofSeconds(2)).usePooling()
				.poolConfig(poolConfig).build();

		// 四、创建RedisConnectionFactory对象，给出redis服务器配置和redis客户端配置
		RedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory(redisServerConfig,
				redisClientConfig);

		// 直接使用RedisConnection来进行和redis数据库的交互
		RedisConnection redisConnection = redisConnectionFactory.getConnection();

		// 向user这个set中添加三个value，其中两个是String对象的二进制数据，另一个是Date对象序列化后的二进制数据
		redisConnection.sAdd("user".getBytes(), "zhangsan".getBytes(), "lisi".getBytes(),
				SerializationUtils.serialize(new Date()));

		// 从redis中获取user这个key的所有member的值
		Set<byte[]> members = redisConnection.sMembers("user".getBytes());
		members.forEach(data -> {
			Object result;
			try {
				result = SerializationUtils.deserialize(data);
			} catch (Exception e) {
				result = new String(data);
			}

			System.out.println(result);
		});

		// 手动关闭redis数据库
		redisConnection.close();

		StringRedisSerializer stirngRedisSerializer = new StringRedisSerializer();
		GenericJackson2JsonRedisSerializer jacksonRedisSerializer = new GenericJackson2JsonRedisSerializer();

		BaseDept dept1 = new BaseDept(20, "No.20", "20公司");
		dept1.setModifyDate(new Date());
		dept1.setIsRun(false);

		BaseDept dept2 = new BaseDept(30, "No.30", "30公司");
		BaseDept dept3 = new BaseDept(40, "No.40", "40公司");

		/*
		 * 使用RedisTemplate和redis数据库进行交互。
		 * 
		 * 由于我们创建的RedisTemplate对象的范型类型被赋值为String和BaseDept，
		 * 因此这个RedisTemplate只处理key是String类型，value是BaseDept类型的数据。
		 * 
		 * 既然已经定好了RedisTemplate处理key和value的类型，
		 * 那么下一步就是根据类型来设置能序列化key和value类型的RedisSerializer。
		 * 
		 * 可以看到，使用RedisTempldate比使用RedisConnection的好处是：
		 * 1.不需要用户手动开启和关闭数据库连接，只需要使用模版进行redis操作即可
		 * 2.不需要关心底层RedisConnection与redis服务器是如何进行通讯的，即使用模版时，客户端不需要进行序列化和反序列化操作。（
		 * 当前，这前提是给RedisTemplate配置好keySerializer、valueSerializer这些）
		 * 
		 * 另外： 当我们使用终端连接redis数据库时，redis永远是把二进制数据解析成字符串，无论这个二进制数据是否为字符串。例如：
		 * 使用RedisTemplate把一个Date对象的jdk序列化结果作为key存放到redis的数据库中。
		 * 那么通过redis终端看到这个key时，也是尝试按照字符串来解析。因此从终端中看到该key的值就像一个乱码似的。
		 * 从终端中看到该key是这样的：
		 * "\xac\xed\x00\x05sr\x00\x0ejava.util.Datehj\x81\x01KYt\x19\x03\x00\x00xpw\b\x00\x00\x01a1\xa0\x16\xe3x"）
		 */
		RedisTemplate<String, BaseDept> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(stirngRedisSerializer);
		redisTemplate.setValueSerializer(jacksonRedisSerializer);

		// 注意：手动创建的模版对象，必须要先调用afterPropertiesSet方法才能使用
		redisTemplate.afterPropertiesSet();

		redisTemplate.opsForValue().set("dept", dept1);
		redisTemplate.boundListOps("deptList").rightPushAll(new BaseDept[] { dept1, dept2, dept3 });

		BaseDept deptRedis = redisTemplate.opsForValue().get("dept");
		List<BaseDept> deptList = redisTemplate.boundListOps("deptList").range(0, -1);

		System.out.println(deptRedis.getDeptName());
		deptList.forEach(theDept -> System.out.println(theDept.getDeptName()));
	}

}
