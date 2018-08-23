package com.spring.aop.logger;

import static org.junit.Assert.assertTrue;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

public class DaoLogger {
	public void logBefore(JoinPoint jp, Object target, Object proxy) {
		assertTrue(jp.getTarget() == target);
		assertTrue(jp.getThis() == proxy);

		System.out.println("DaoLogger.logBefore():" + jp.getSignature());
	}

	public void logAfter(JoinPoint jp, Object target, Object proxy) {
		assertTrue(jp.getTarget() == target);
		assertTrue(jp.getThis() == proxy);

		System.out.println("DaoLogger.logAfter():" + jp.getSignature());
	}

	public void logAfterReturning(JoinPoint jp, Object result, Object target, Object proxy) {
		assertTrue(jp.getTarget() == target);
		assertTrue(jp.getThis() == proxy);

		System.out.println("DaoLogger.logAfterReturning():" + jp.getSignature());
	}

	public void logOnException(JoinPoint jp, Exception exception, Object target, Object proxy) {
		assertTrue(jp.getTarget() == target);
		assertTrue(jp.getThis() == proxy);

		System.out.println("DaoLogger.logOnException():" + jp.getSignature());
	}

	public Object logAround(ProceedingJoinPoint pjp, Object target, Object proxy) throws Throwable {
		assertTrue(pjp.getTarget() == target);
		assertTrue(pjp.getThis() == proxy);

		System.out.println("DaoLogger.logAround():" + pjp.getSignature());
		Object result = pjp.proceed();
		return result;
	}
}
