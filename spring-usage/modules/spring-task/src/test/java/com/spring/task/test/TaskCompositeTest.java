package com.spring.task.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.spring.task.service.TaskCompositeService;

public class TaskCompositeTest {

	@Test
	@Ignore
	public void test1() throws InterruptedException, ExecutionException {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/taskComposite.xml")) {
			TaskCompositeService taskService = context.getBean(TaskCompositeService.class);
			taskService.asyncTask2("helloWorld");
			taskService.asyncTask2("zhangsan");

			taskService.asyncTask2("helloWorld");
			taskService.asyncTask2("zhangsan");

			taskService.asyncTask1();
			taskService.asyncTask1();

			taskService.asyncTask2("helloWorld");
			taskService.asyncTask2("zhangsan");

			taskService.asyncTask2("lisi");
			taskService.asyncTask2("wangwu");

			taskService.asyncTask1();
			taskService.asyncTask1();

			taskService.asyncTask2("lisi");
			taskService.asyncTask2("wangwu");

			Future<Integer> asyncResult = taskService.asyncTask3(5, 9);
			Integer result = null;

			try {
				result = asyncResult.get(5, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
			System.out.println(result);
		}
	}

	@Test
	public void test2() throws InterruptedException, ExecutionException {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/taskComposite.xml")) {
			TaskCompositeService taskService = context.getBean(TaskCompositeService.class);
			taskService.asyncTask4(5, 0);

			// 注意：这个是向TaskExecutor提交任务时，TaskExecutor返回的Future对象，而不是asyncTask5方法返回的Future对象
			Future<String> result = taskService.asyncTask5("你好", "hello");

			String test = result.get();
			System.out.println(test);

			try {
				Thread.sleep(100000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}