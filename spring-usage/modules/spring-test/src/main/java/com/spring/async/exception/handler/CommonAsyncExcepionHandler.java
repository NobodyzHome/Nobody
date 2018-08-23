package com.spring.async.exception.handler;

import java.lang.reflect.Method;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

public class CommonAsyncExcepionHandler implements AsyncUncaughtExceptionHandler {

	/*
	 * spring是在一个新的线程中执行@Async方法，那么当执行@Async方法发生异常时，根据@Async方法是否有返回值，
	 * 对于异常的处理会有不同的方式：
	 * 
	 * 1.如果一个@Async方法有返回值，那么它的返回值必须是Future类型的。这样，在单独一个线程执行该方法时产生异常，那么这个线程则计算完毕，
	 * 当主线程在调用该方法返回的Future对象的get方法时，会接收到子线程执行@Async方法时产生的异常，此时主线程应该处理该异常。
	 * 
	 * 2.如果一个@Async方法没有返回值，那么在单独一个线程中执行该方法产生异常时，
	 * 则是在这个线程中将异常交由AsyncUncaughtExceptionHandler来处理。
	 * 
	 * 要注意这两种@Async方法处理异常的区别，主要区别在于对于有返回值的@Async方法，当它在子线程执行发生异常时，那么子线程就直接结束该方法，
	 * 等主线程调用返回的Future对象的get方法时，会接到@Async方法在子线程执行时发生的异常。相当于此时是主线程处理子线程执行时发生的异常。
	 * 而对于没有返回值的@Async方法，当在子线程执行发生异常时，
	 * 会在子线程中将该异常交由AsyncUncaughtExceptionHandler来处理，也就是说此时是在子线程中处理该异常，
	 * 主线程无需处理这个异常。
	 */
	@Override
	public void handleUncaughtException(Throwable ex, Method method, Object... params) {
		System.out.println("抛出的异常是：" + ex);
		System.out.println("调用的方法是：" + method + "，参数是：" + ArrayUtils.toString(params));
	}

}
