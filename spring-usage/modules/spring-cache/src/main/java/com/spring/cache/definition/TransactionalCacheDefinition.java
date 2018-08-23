package com.spring.cache.definition;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;

@Component
@ImportResource("classpath*:spring/data-access.xml")
public class TransactionalCacheDefinition {

	@Bean
	@Qualifier
	public ConcurrentMapCacheManager jdkCacheManager(@Value("hello,world,test") String[] cacheNames) {
		ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(cacheNames);
		cacheManager.setAllowNullValues(true);

		return cacheManager;
	}

	@Bean
	public EhCacheManagerFactoryBean ehcacheCacheManager(@Value("classpath:ehcache/ehcache3.xml") Resource config) {
		EhCacheManagerFactoryBean ehCacheCachemanager = new EhCacheManagerFactoryBean();
		ehCacheCachemanager.setConfigLocation(config);

		return ehCacheCachemanager;
	}

	@Bean(autowire = Autowire.BY_TYPE)
	@Qualifier
	public EhCacheCacheManager springEhcacheManager() {
		EhCacheCacheManager ehCacheManager = new EhCacheCacheManager();
		return ehCacheManager;
	}

	@Bean
	public CompositeCacheManager compositeCacheManager(@Qualifier CacheManager... cacheManagers) {
		CompositeCacheManager cacheManager = new CompositeCacheManager(cacheManagers);
		cacheManager.setFallbackToNoOpCache(true);

		return cacheManager;
	}

	@Bean
	@Primary
	public TransactionAwareCacheManagerProxy transactionalCacheManager(CacheManager compositeCacheManager) {
		TransactionAwareCacheManagerProxy transactionalCacheManager = new TransactionAwareCacheManagerProxy(
				compositeCacheManager);
		transactionalCacheManager.setTargetCacheManager(compositeCacheManager);

		return transactionalCacheManager;
	}

	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("validation/error");

		return messageSource;
	}

	@Bean
	public OptionalValidatorFactoryBean validator(MessageSource messageSource) {
		OptionalValidatorFactoryBean validator = new OptionalValidatorFactoryBean();
		validator.setValidationMessageSource(messageSource);
		
		return validator;
	}

	@Bean(autowire = Autowire.BY_TYPE)
	public static MethodValidationPostProcessor methodValidationPostProcessor() {
		MethodValidationPostProcessor validationProcessor = new MethodValidationPostProcessor();

		return validationProcessor;
	}

}