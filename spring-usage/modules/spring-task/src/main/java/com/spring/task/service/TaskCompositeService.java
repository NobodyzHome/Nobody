package com.spring.task.service;

import java.util.concurrent.Future;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.hibernate.validator.constraints.Length;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;

@CacheConfig(cacheNames = "hello")
@Validated
public class TaskCompositeService {

	/*
	 * 注意：@Scheduled方法和@Async方法的区别
	 * 
	 * 1.@Scheduled方法只能是没有参数，返回值为void的方法
	 * 
	 * Notice that the methods to be scheduled must have void returns and must
	 * not expect any arguments. If the method needs to interact with other
	 * objects from the Application Context, then those would typically have
	 * been provided through dependency injection.
	 * 
	 * 2.@Async方法可以有参数，也可以没有。同时@Async也可以有返回值，但返回值必须是Future类型的。
	 * 注意：可以通过commons-lang包的ConcurrentUtils.constantFuture方法来创建一个常量的Future对象。
	 * 
	 * Unlike the methods annotated with the @Scheduled annotation, these
	 * methods can expect arguments, because they will be invoked in the
	 * "normal" way by callers at runtime rather than from a scheduled task
	 * being managed by the container. Even methods that return a value can be
	 * invoked asynchronously. However, such methods are required to have a
	 * Future typed return value.
	 */
	@Scheduled(initialDelay = 3000, fixedRate = 2000)
	public void scheduleTask1() {
		System.out.println("TaskCompositeService.scheduleTask1()");
	}

	@Scheduled(fixedDelay = 1000)
	public void scheduleTask2() {
		System.out.println("TaskCompositeService.scheduleTask2()");
	}

	// 该方法是通过task:scheduleTasks配置的，因此没有加@Scheduled注解
	public void scheduleTask3() {
		System.out.println("TaskCompositeService.scheduleTask3()");
	}

	@Async
	@CacheEvict(cacheResolver = "cacheResolver", allEntries = true)
	public void asyncTask1() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("TaskCompositeService.asyncTask1()");
	}

	@Async
	@Caching(cacheable = { @Cacheable, @Cacheable("world") })
	public void asyncTask2(String name) {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("TaskCompositeService.asyncTask2():" + name);
	}

	@Async
	public Future<Integer> asyncTask3(Integer a, Integer b) {
		Integer result = a * b;
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 使用ConcurrentUtils.constantFuture创建一个常量Future
		Future<Integer> constantResult = ConcurrentUtils.constantFuture(result);
		return constantResult;
	}

	/*
	 * 当将一个任务提交给TaskExecutor后，则会使用一个新线程来执行这个任务。而如果在新线程中执行这个任务发生异常时，则会有以下处理异常的办法：
	 * 
	 * 1.如果@Async方法没有返回值，那么在新线程中执行该方法发生的异常，会被task:annotation-config中的exception-
	 * handler处理。也就是会被指定的AsyncUncaughtExceptionHandler的实现类的实例来处理异步任务执行时发生的异常。
	 * 
	 * 2.如果@Async方法有返回值，那么在新线程中执行该方法发生的异常，会在主线程获得TaskExecutor返回的Future对象后，
	 * 调用Future对象的get方法时，抛出异常。注意：此时则不会被AsyncUncaughtExceptionHandler来处理异常。
	 * 
	 * When an @Async method has a Future typed return value, it is easy to
	 * manage an exception that was thrown during the method execution as this
	 * exception will be thrown when calling get on the Future result. With a
	 * void return type however, the exception is uncaught and cannot be
	 * transmitted. For those cases, an AsyncUncaughtExceptionHandler can be
	 * provided to handle such exceptions.
	 */
	@Async
	public void asyncTask4(Integer divisor, @Min(1) Integer dividend) {
		System.out.println(divisor / dividend);
	}

	@Async
	public Future<String> asyncTask5(@Length(min = 2, max = 4) String firstName, @NotBlank String lastName) {
		String fullName = firstName + " " + lastName;
		return ConcurrentUtils.constantFuture(fullName);
	}
}
