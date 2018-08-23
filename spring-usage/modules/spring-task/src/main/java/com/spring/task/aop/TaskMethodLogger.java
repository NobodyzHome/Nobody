package com.spring.task.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class TaskMethodLogger {

	@Pointcut("target(target) && this(proxy)")
	public void targetAndProxy(Object target, Object proxy) {
	}

	@Pointcut("execution(@org.springframework.scheduling.annotation.Scheduled public void com.spring.task..*Service.*()) "
			+ "&& targetAndProxy(target,proxy)")
	public void scheduledMethods(Object target, Object proxy) {
	}

	@Pointcut("execution(@org.springframework.scheduling.annotation.Async public * com.spring.task..*Service.*(..)) && "
			+ "targetAndProxy(target,proxy)")
	public void asyncMethods(Object target, Object proxy) {
	}

	@Before(value = "scheduledMethods(target,proxy)", argNames = "target,proxy")
	public void logScheduleMethods(JoinPoint jp, Object target, Object proxy) {
		System.out.println(jp.getSignature());
	}

	@Before(value = "asyncMethods(target,proxy)", argNames = "target,proxy")
	public void logAsyncMethods(JoinPoint jp, Object target, Object proxy) {
		System.out.println(jp.getSignature());
	}

	@AfterReturning(pointcut = "asyncMethods(target,proxy)", returning = "result", argNames = "target,proxy,result")
	public void logAsyncMethodsResult(JoinPoint jp, Object target, Object proxy, Object result) {
		System.out.println(jp.getSignature() + ":" + result);
	}

	@AfterThrowing(pointcut = "asyncMethods(target,proxy)", throwing = "exception", argNames = "exception,target,proxy")
	public void logAsyncMethodsException(JoinPoint jp, Exception ex, Object target, Object proxy) {
		System.out.println(jp.getSignature().getName() + "发生异常：" + ex.getMessage());
	}

}