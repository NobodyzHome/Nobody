package com.spring.aop.log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

@Aspect
@Order(5)
public class AnnotationControllerMethodLogger {

	@Pointcut("execution(@org.springframework.web.bind.annotation.*Mapping public * "
			+ "(@(org.springframework.web.bind.annotation.RestController || org.springframework.stereotype.Controller) "
			+ "com.spring.web.controller..*Controller).*(..))")
	public void controllerMethods() {
	}

	@Before("controllerMethods()")
	public void logBeforeOperation() {
		System.out.println("AnnotationControllerMethodLogger.logBeforeOperation()");
	}

	@After("controllerMethods()")
	public void logAfterOperation() {
		System.out.println("AnnotationControllerMethodLogger.logAfterOperation()");
	}

	@AfterReturning(pointcut = "controllerMethods()", returning = "result", argNames = "result")
	public void logReturningOperation(JoinPoint jp, Object result) {
		System.out.println("AnnotationControllerMethodLogger.logReturningOperation():" + result);
	}

	@AfterThrowing(pointcut = "controllerMethods()", throwing = "ex", argNames = "ex")
	public void logThrowingOperation(Exception ex) {
		System.out.println("AnnotationControllerMethodLogger.logThrowingOperation():" + ex.getMessage());
	}

	@Around(value = "controllerMethods()")
	public Object logArroundOperation(ProceedingJoinPoint pjp) throws Throwable {
		System.out.println("AnnotationControllerMethodLogger.logArroundOperation()");
		Object result = pjp.proceed();
		return result;
	}
}
