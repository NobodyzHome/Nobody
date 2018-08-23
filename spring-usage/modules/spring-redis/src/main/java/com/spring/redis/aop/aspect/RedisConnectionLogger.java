package com.spring.redis.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;

@Aspect
public class RedisConnectionLogger {

	@Pointcut("execution(org.springframework.data.redis.connection.RedisConnection"
			+ " org.springframework.data.redis.connection.RedisConnectionFactory.getConnection(..)) && target(connFactory)")
	public void getConnection(RedisConnectionFactory connFactory) {
	}

	@AfterReturning(value = "getConnection(connFactory)", returning = "conn", argNames = "connFactory,conn")
	public void logConn(JoinPoint jp, RedisConnectionFactory connFactory, RedisConnection conn) {
		if (conn instanceof JedisConnection) {
			JedisConnection jedisConn = (JedisConnection) conn;
			System.out.println(conn + "@@@@@@@@" + jedisConn.getJedis());
		} else {
			System.out.println(conn);
		}
	}
}
