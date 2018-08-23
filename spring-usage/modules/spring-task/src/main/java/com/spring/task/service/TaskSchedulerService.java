package com.spring.task.service;

import org.springframework.scheduling.annotation.Scheduled;

public class TaskSchedulerService {

	/*
	 * spring对@Scheduled的处理和对@Async的处理是不同的：
	 * 在容器开启后，spring就会找到容器中注册的<bean>的@Scheduled方法，根据@Scheduled的配置，使用<bean>
	 * 对象和这个方法来创建一个Runnable对象。然后将这个Runnable对象提交给task:annotation-
	 * driven中指定的scheduler，由这个scheduler来执行任务。注意：在这个过程中，并没有对<bean>的类创建代理。
	 * 
	 * 而容器对注册的<bean>的@Async方法，实际是创建这个<bean>的代理类，然后在@Async方法的责任链对象中注册一个
	 * AnnotationAsyncExecutionInterceptor。
	 * AnnotationAsyncExecutionInterceptor会将调用连接点方法对应的责任链对象的proceed方法创建成一个Runnable对象，
	 * 然后将这个Runnable对象提交给task:annotation-driven中指定的executor，
	 * 来在一个新线程中执行这个Runnable对象。即外部调用@Async方法时，实际是在一个新线程中调用这个方法。
	 */
	@Scheduled(fixedRate = 2000)
	public void method1() {
		System.out.println("TaskSchedulerService.method1()");
	}

	/*
	 * 注意：@Scheduled方法必须是无入参，无返回值的方法。而@Async方法可以有入参，也可以有返回值，但返回值的类型必须是Future类型的。
	 */
	@Scheduled(initialDelay = 1000, fixedDelay = 2000)
	public void method2() {
		System.out.println("TaskSchedulerService.method2()");
	}

	public void method3() {
		System.out.println("TaskSchedulerService.method3()");
	}
}
