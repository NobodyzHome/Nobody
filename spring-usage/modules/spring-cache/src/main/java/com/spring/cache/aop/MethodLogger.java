package com.spring.cache.aop;

import java.util.StringJoiner;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@EnableAspectJAutoProxy(exposeProxy = true)
// @Order注解中的值决定了责任链中该通知对象的存放位置，值越小越靠前，责任链就会越先执行这个通知对象
@Order(Ordered.LOWEST_PRECEDENCE)
public class MethodLogger {

	@Pointcut("execution(@org.springframework.cache.annotation.* public * com.spring.cache.*..*Cache*+.*(..))")
	public void cacheMethods() {
	}

	@After("cacheMethods()")
	public void logMethodName(JoinPoint jp) {
		StringJoiner contents = new StringJoiner("\n");
		contents.add(jp.getSignature().toString());

		System.out.println(contents);
	}
}
