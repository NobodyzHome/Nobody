package com.spring.redis.registration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

public class RedisConnectionFactoryBean implements FactoryBean<RedisConnectionFactory> {

	private RedisConnectionFactory redisConnectionFactory;

	private String hostName;
	private Integer port;
	private Integer dbIndex;
	private Boolean usePool;
	private Integer minIdle;
	private Integer maxIdle;
	private Integer maxTotal;
	private Integer maxWaitSeconds;
	private Integer minEvictableSeconds;

	@Override
	public RedisConnectionFactory getObject() throws Exception {
		return redisConnectionFactory;
	}

	@Override
	public Class<?> getObjectType() {
		return RedisConnectionFactory.class;
	}

	public RedisConnectionFactory getRedisConnectionFactory() {
		return redisConnectionFactory;
	}

	public void setRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory) {
		this.redisConnectionFactory = redisConnectionFactory;
	}

	public String getHostname() {
		return hostName;
	}

	public void setHostname(String hostName) {
		this.hostName = hostName;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getDbIndex() {
		return dbIndex;
	}

	public void setDbIndex(Integer dbIndex) {
		this.dbIndex = dbIndex;
	}

	public Boolean getUsePool() {
		return usePool;
	}

	public void setUsePool(Boolean usePool) {
		this.usePool = usePool;
	}

	public Integer getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(Integer minIdle) {
		this.minIdle = minIdle;
	}

	public Integer getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(Integer maxIdle) {
		this.maxIdle = maxIdle;
	}

	public Integer getMaxTotal() {
		return maxTotal;
	}

	public void setMaxTotal(Integer maxTotal) {
		this.maxTotal = maxTotal;
	}

	public Integer getMaxWaitSeconds() {
		return maxWaitSeconds;
	}

	public void setMaxWaitSeconds(Integer maxWaitSeconds) {
		this.maxWaitSeconds = maxWaitSeconds;
	}

	public Integer getMinEvictableSeconds() {
		return minEvictableSeconds;
	}

	public void setMinEvictableSeconds(Integer minEvictableSeconds) {
		this.minEvictableSeconds = minEvictableSeconds;
	}

	@PostConstruct
	private void createRedisConnectionFactory() {
		RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration(hostName, port);
		redisConfiguration.setDatabase(dbIndex);

		JedisClientConfigurationBuilder jedisClientBuilder = JedisClientConfiguration.builder().clientName("maziqiang")
				.connectTimeout(Duration.ofMinutes(5));

		if (usePool) {
			GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
			poolConfig.setMinIdle(minIdle);
			poolConfig.setMaxIdle(maxIdle);
			poolConfig.setMaxTotal(maxTotal);
			poolConfig.setMaxWaitMillis(TimeUnit.SECONDS.toMillis(maxWaitSeconds));
			poolConfig.setMinEvictableIdleTimeMillis(TimeUnit.SECONDS.toMillis(minEvictableSeconds));

			jedisClientBuilder.usePooling().poolConfig(poolConfig);
		}
		JedisClientConfiguration clientConfiguration = jedisClientBuilder.build();

		redisConnectionFactory = new JedisConnectionFactory(redisConfiguration, clientConfiguration);
	}
}
