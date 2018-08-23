package com.spring.cache.definition;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.NamedCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = { "com.spring.cache.jdk", "com.spring.cache.aop" })
public class CacheDefinition {

	@Bean
	public CacheManager cacheManager(@Value("dept_region,date_region,line_region") String... cacheNames) {
		ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(cacheNames);
		return cacheManager;
	}

	@Bean
	public CacheResolver cacheResolver(@Value("dept_region,date_region") String... cacheNames) {
		CacheManager cacheManager = cacheManager();
		NamedCacheResolver cacheResolver = new NamedCacheResolver(cacheManager, cacheNames);

		return cacheResolver;
	}

	/*
	 * 注意：对于注解方式的spring配置文件，也要通过@ComponentScan的方式，将含有@EnableCaching等@
	 * Enable系列的注解的类注册成<bean>，而不是直接使用@Bean的方式将这些类注册到容器中。因为直接使用@
	 * Bean的方式将这些类注册到容器中，会导致类上的@EnableCaching等@Enable系列的注解无效。
	 */
	// @Bean
	// public ConcurrentMapCacheAnnotationService cacheService() {
	// return new ConcurrentMapCacheAnnotationService();
	// }
	//
	// @Bean
	// public MethedLogger methodLogger() {
	// return new MethedLogger();
	// }
}
