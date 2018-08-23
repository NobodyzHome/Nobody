package com.spring.task.test;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.spring.task.service.TaskSchedulerService;

public class TaskSchedulerTest {

	@Test
	public void test1() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/taskScheduler.xml")) {
			TaskSchedulerService service = context.getBean(TaskSchedulerService.class);
			service.method1();
			try {
				Thread.sleep(10000000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
