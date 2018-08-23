package com.spring.cache.definition;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.NamedCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@ComponentScan({ "com.spring.cache.ehcache", "com.spring.cache.aop" })
public class EhCacheDefinition {

	@Bean
	public EhCacheManagerFactoryBean ehcacheManager(@Value("classpath:ehcache/ehcache1.xml") Resource ehConfig) {
		EhCacheManagerFactoryBean ehcacheManager = new EhCacheManagerFactoryBean();
		ehcacheManager.setConfigLocation(ehConfig);

		return ehcacheManager;
	}

	@Bean(autowire = Autowire.BY_TYPE)
	public EhCacheCacheManager springEhCacheManager() {
		EhCacheCacheManager ehCacheManager = new EhCacheCacheManager();
		return ehCacheManager;
	}

	@Bean
	public CacheResolver specifiedCacheResolver(@Value("line_region") String[] cacheNames, CacheManager cacheManager) {
		NamedCacheResolver cacheResolver = new NamedCacheResolver();
		cacheResolver.setCacheNames(Arrays.asList(cacheNames));
		cacheResolver.setCacheManager(cacheManager);

		return cacheResolver;
	}
}