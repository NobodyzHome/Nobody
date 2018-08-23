package com.spring.aop.logger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.transaction.PlatformTransactionManager;

@Aspect
public class TransactionManagerLogger {

	@Pointcut("execution(public * org.springframework.transaction.PlatformTransactionManager.*(..) "
			+ "throws org.springframework.transaction..Transaction*Exception) && target(target) && this(proxy)")
	public void transactionMethods(PlatformTransactionManager target, PlatformTransactionManager proxy) {
	}

	@Before(value = "transactionMethods(target,proxy)", argNames = "target,proxy")
	public void logOnBefore(JoinPoint jp, PlatformTransactionManager target, PlatformTransactionManager proxy) {
		System.out.println("TransactionManagerLogger.logOnBefore():" + jp.getSignature().getName());
	}

	@After(value = "transactionMethods(target,proxy) && args(arg1,..)", argNames = "target,arg1,proxy")
	public void logOnAfter(JoinPoint jp, PlatformTransactionManager target, Object param,
			PlatformTransactionManager proxy) {
		System.out.println("TransactionManagerLogger.logOnAfter():" + param + ":" + jp.getSignature().getName());
	}

	@AfterThrowing(pointcut = "transactionMethods(target,proxy) && args(arg1,..)", throwing = "ex", argNames = "target,proxy,arg1,ex")
	public void logOnAfterThrowing(JoinPoint jp, PlatformTransactionManager target, PlatformTransactionManager proxy,
			Object param, Exception ex) {
		System.out.println("TransactionManagerLogger.logOnAfterThrowing():" + ex + ":" + jp.getSignature().getName());
	}

	@AfterReturning(pointcut = "transactionMethods(target,proxy)", returning = "result", argNames = "result,proxy,target")
	public void logOnAfterReturing(JoinPoint jp, Object result, PlatformTransactionManager proxy,
			PlatformTransactionManager target) {
		System.out
				.println("TransactionManagerLogger.logOnAfterReturing():" + result + ":" + jp.getSignature().getName());
	}

	@Around(value = "transactionMethods(target,proxy)", argNames = "target,proxy")
	public Object logAround(ProceedingJoinPoint pjp, PlatformTransactionManager target,
			PlatformTransactionManager proxy) throws Throwable {
		System.out.println("TransactionManagerLogger.logAround():" + pjp.getSignature().getName());
		Object result = pjp.proceed();
		return result;
	}
}
