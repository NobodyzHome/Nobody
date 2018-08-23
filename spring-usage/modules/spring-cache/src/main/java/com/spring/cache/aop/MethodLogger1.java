package com.spring.cache.aop;

import org.aspectj.lang.JoinPoint;
import org.junit.Assert;

public class MethodLogger1 {

	public void logBeforeInvocation(JoinPoint jp, Object target, Object proxy) {
		System.err.println(jp.getSignature());
		Assert.assertEquals(jp.getTarget(), target);
		Assert.assertEquals(jp.getThis(), proxy);
	}

	public void logOnException(JoinPoint jp, Exception exception) {
		System.err.println(jp.getSignature().getName() + " 发生错误：" + exception);
	}
}
