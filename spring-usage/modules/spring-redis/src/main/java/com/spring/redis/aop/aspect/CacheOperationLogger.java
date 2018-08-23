package com.spring.redis.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.cache.annotation.Cacheable;

@Aspect
public class CacheOperationLogger {

	@Pointcut("@annotation(org.springframework.cache.annotation.Cacheable) && target(target) && this(proxy) && @annotation(cacheable)")
	public void cacheOperationPointCut(Object target, Object proxy, Cacheable cacheable) {
	}

	@Around(value = "cacheOperationPointCut(target,proxy,cacheable)", argNames = "target,proxy,cacheable")
	public Object aroundAdvice(ProceedingJoinPoint pjp, Object target, Object proxy, Cacheable cacheable)
			throws Throwable {
		System.out.println(pjp.getSignature());
		Object result = pjp.proceed();
		return result;
	}
}
