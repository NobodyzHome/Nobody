package com.spring.aop.log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

public class ControllerMethodLogger {

	public void logBeforeOperation(JoinPoint jp) {
		System.out.println("ControllerMethodLogger.logBeforeOperation()");
	}

	public void logAfterOperation(JoinPoint jp) {
		System.out.println("ControllerMethodLogger.logAfterOperation()");
	}

	public void logThrowingOperation(JoinPoint jp, Exception exception) {
		System.out.println("ControllerMethodLogger.logThrowingOperation()");
	}

	public void logReturningOperation(JoinPoint jp, Object returning) {
		System.out.println("ControllerMethodLogger.logReturningOperation()");
	}

	public Object logArround(ProceedingJoinPoint pjp) throws Throwable {
		System.out.println("ControllerMethodLogger.logArround()");
		Object result = pjp.proceed();
		return result;
	}
}
