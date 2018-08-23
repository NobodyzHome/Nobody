package com.spring.aop.logger;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

public class DataSourceLogger {

	public void logBeforeConnection(JoinPoint jp, DataSource target, DataSource proxy) {
		System.out.println("DataSourceLogger.logBeforeConnection():" + jp.getSignature().getName());
	}

	public void logAfterConnection(JoinPoint jp, DataSource target, DataSource proxy) {
		System.out.println("DataSourceLogger.logAfterConnection():" + jp.getSignature().getName());
	}

	public void logAfterReturningConnection(JoinPoint jp, Connection conn, DataSource target, DataSource proxy) {
		System.out
				.println("DataSourceLogger.logAfterReturningConnection():" + conn + ":" + jp.getSignature().getName());
	}

	public void logOnSqlException(JoinPoint jp, SQLException sqlEx, DataSource target, DataSource proxy) {
		System.out.println("DataSourceLogger.logOnSqlException():" + sqlEx + jp.getSignature().getName());
	}

	public Object logAround(ProceedingJoinPoint pjp, DataSource target, DataSource proxy) throws Throwable {
		System.out.println("DataSourceLogger.logAround():" + pjp.getSignature().getName());
		Object result = pjp.proceed();
		return result;
	}
}