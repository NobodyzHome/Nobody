package com.spring.cache.definition;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.NamedCacheResolver;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@ComponentScan(basePackages = { "com.spring.cache.aop", "com.spring.cache.composite" })
public class CompositeCacheDefinition {

	@Bean
	public ConcurrentMapCacheManager jdkCacheManager(@Value("date_region") String... cacheNames) {
		ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(cacheNames);
		cacheManager.setAllowNullValues(true);

		return cacheManager;
	}

	@Bean
	public EhCacheManagerFactoryBean ehCacheManager(@Value("classpath:ehcache/ehcache2.xml") Resource cacheConfig) {
		EhCacheManagerFactoryBean ehCacheManager = new EhCacheManagerFactoryBean();
		ehCacheManager.setConfigLocation(cacheConfig);
		ehCacheManager.setAcceptExisting(true);
		return ehCacheManager;
	}

	@Bean(autowire = Autowire.BY_TYPE)
	public EhCacheCacheManager springEhCacheManager() {
		EhCacheCacheManager cacheManager = new EhCacheCacheManager();

		return cacheManager;
	}

	@Bean
	@Primary
	@Qualifier("cacheManagerUsed")
	public CacheManager compositeCacheManager(CacheManager... cacheManagers) {
		CompositeCacheManager cacheManager = new CompositeCacheManager(cacheManagers);
		cacheManager.setFallbackToNoOpCache(true);

		return cacheManager;
	}

	@Bean
	public CacheResolver specifiedCacheResolver(@Qualifier("cacheManagerUsed") CacheManager cacheManager,
			@Value("date_region,dept_region") String... cacheNames) {
		NamedCacheResolver cacheResolver = new NamedCacheResolver(cacheManager, cacheNames);

		return cacheResolver;
	}
}
