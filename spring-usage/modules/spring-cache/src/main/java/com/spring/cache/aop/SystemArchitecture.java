package com.spring.cache.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class SystemArchitecture {

	@Pointcut("execution(@org.springframework.cache.annotation.* public * com.spring.cache.*.*Cache*.*(..))")
	public void cacheMethods() {
	}

	/*
	 * 一定要注意：在aspectJ的切入点中，使用“&&,||,!”作为逻辑关系操作，而在xml的切入点中，使用"and,or,not"作为逻辑操作。
	 * 例如：aspectJ的切入点为"target(target) && this(proxy)"
	 * ，但xml对应的切入点应为"target(target) and this(proxy)"
	 */
	@Pointcut("cacheMethods() && target(target)")
	public void cacheMethodsWithTarget(Object target) {

	}

	@Pointcut("cacheMethodsWithTarget(target) && this(proxy)")
	public void cacheMethodsWithProxy(Object target, Object proxy) {
	}

}
