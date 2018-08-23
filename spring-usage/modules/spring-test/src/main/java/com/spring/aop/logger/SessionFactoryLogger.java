package com.spring.aop.logger;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class SessionFactoryLogger {

	@Pointcut("execution(public org.apache.ibatis..session.SqlSession+ org.apache.ibatis.session.SqlSessionFactory+.openSession(..))"
			+ " && target(target) ")
	public void openSessionMethods(SqlSessionFactory target) {
	}

	@Pointcut("openSessionMethods(target) && this(proxy)")
	public void openSessionMethods1(SqlSessionFactory target, SqlSessionFactory proxy) {
	}

	@Before(value = "openSessionMethods1(target,proxy)", argNames = "target,proxy")
	public void logBefore(JoinPoint jp, SqlSessionFactory target, SqlSessionFactory proxy) {
		System.out.println("SessionFactoryLogger.logBefore():" + jp.getSignature());
	}

	@After(value = "openSessionMethods1(target,proxy)", argNames = "target,proxy")
	public void logAfter(JoinPoint jp, SqlSessionFactory target, SqlSessionFactory proxy) {
		System.out.println("SessionFactoryLogger.logAfter():" + jp.getSignature());
	}

	@AfterReturning(value = "openSessionMethods1(target,proxy)", argNames = "result,target,proxy", returning = "result")
	public void logAfterReturning(JoinPoint jp, SqlSession session, SqlSessionFactory target, SqlSessionFactory proxy) {
		System.out.println("SessionFactoryLogger.logAfterReturning():" + session);
	}

	@AfterThrowing(value = "openSessionMethods1(target,proxy)", throwing = "ex", argNames = "ex,target,proxy")
	public void logThrowing(JoinPoint jp, Exception ex, SqlSessionFactory target, SqlSessionFactory proxy) {
		System.out.println("SessionFactoryLogger.logThrowing():" + ex);
	}

	@Around(value = "openSessionMethods1(target,proxy)", argNames = "target,proxy")
	public Object logAround(ProceedingJoinPoint pjp, SqlSessionFactory target, SqlSessionFactory proxy)
			throws Throwable {
		System.out.println("SessionFactoryLogger.logAround()");
		return pjp.proceed();
	}
}
