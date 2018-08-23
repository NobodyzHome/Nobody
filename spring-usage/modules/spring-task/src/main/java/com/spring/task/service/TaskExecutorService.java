package com.spring.task.service;

import java.util.concurrent.CountDownLatch;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TaskExecutorService {

	@Async
	public void executeWithDefaultExecutor(CountDownLatch countDown) {
		Thread.dumpStack();

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		countDown.countDown();
	}

	@Async("syncTaskExecutor")
	public void executeWithSyncExecutor(CountDownLatch countDown) {
		executeWithDefaultExecutor(countDown);
	}

	@Async("simpleAsyncTaskExecutor")
	public void executeWithSimpleAsyncExecutor(CountDownLatch countDown) {
		executeWithDefaultExecutor(countDown);
	}

	@Async("threadPoolExecutor")
	public void executeWithThreadPoolExecutor(CountDownLatch countDown) {
		executeWithDefaultExecutor(countDown);
	}

	@Async("concurrentTaskExecutor")
	public void executeWithConcurrentTaskExecutor(CountDownLatch countDown) {
		executeWithDefaultExecutor(countDown);
	}

	@Async("threadPoolTaskExecutor")
	public void executeWithThreadPoolTaskExecutor(CountDownLatch countDown) {
		executeWithDefaultExecutor(countDown);
	}

	@Async("threadPoolTaskExecutor1")
	public void executeWithThreadPoolTaskExecutor1(CountDownLatch countDown) {
		executeWithDefaultExecutor(countDown);
	}
}