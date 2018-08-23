package com.spring.task.test;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.spring.task.service.TaskExecutorService;

public class TaskExecutorTest {

	@Test
	public void test1() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/taskExecutor.xml")) {
			TaskExecutorService service = context.getBean(TaskExecutorService.class);

			int count = 10;
			CountDownLatch countDown = new CountDownLatch(count);

			while (count-- > 0) {
				// service.executeWithDefaultExecutor(countDown);
				// service.executeWithSyncExecutor(countDown);
				// service.executeWithSimpleAsyncExecutor(countDown);
				// service.executeWithThreadPoolExecutor(countDown);
				// service.executeWithConcurrentTaskExecutor(countDown);
				service.executeWithThreadPoolTaskExecutor(countDown);
				// service.executeWithThreadPoolTaskExecutor1(countDown);
			}

			countDown.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
