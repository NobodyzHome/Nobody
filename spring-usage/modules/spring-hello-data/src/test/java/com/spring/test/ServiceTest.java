package com.spring.test;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;

import com.spring.data.domain.BaseDept;
import com.spring.data.domain.BaseLine;
import com.spring.data.service.BaseDeptService;
import com.spring.data.service.BaseLineService;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/container/busness-service.xml")
@ActiveProfiles({ "embedded", "test" })
@TestPropertySource(properties = { "year=2017", "month=11", "startTime=${year}-${month}-20 15:30:51",
		"endTime=${year}-${month}-19 13:30:21", "deptNo=3" })
public class ServiceTest {

	@Autowired
	private BaseDeptService deptService;

	@Autowired
	private BaseLineService lineService;

	@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	@Value("${startTime}")
	private Date startTime;

	@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	@Value("${endTime}")
	private Date endTime;

	@Resource(name = "threadPoolTaskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;

	@Autowired
	private Environment environment;

	@Value("${deptNo}")
	private Integer deptNo;

	@SuppressWarnings("unused")
	@Test
	@Ignore
	public void test1() {
		Integer deptNo = Integer.valueOf(environment.resolveRequiredPlaceholders("${deptNo}"));

		List<BaseDept> dept1 = deptService.findByExample(new BaseDept(deptNo, null));
		List<BaseDept> dept2 = deptService.findByExample(new BaseDept(deptNo, null));

		// try {
		// Thread.sleep(5000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

		List<BaseDept> dept3 = deptService.findByExample(new BaseDept(deptNo, null));
		List<BaseDept> dept4 = deptService.findByExample(new BaseDept(deptNo, null));

		List<BaseDept> dept5 = deptService.findByExample1(deptNo);
		System.out.println(dept5);

		// 因为不满足@Cacheable的condition条件，因此该参数值的结果不会被缓存
		Integer timeInterval1 = deptService.timeInterval(endTime, startTime);
		Integer timeInterval2 = deptService.timeInterval(endTime, startTime);

		System.out.println(timeInterval1);

		// 以下两行只会执行一次连接点方法，因为连接点结果不满足unless属性，因此能存入缓存中。
		BaseDept dept6 = deptService.findById(1);
		BaseDept dept7 = deptService.findById(1);

		// 由于满足了@Cacheable的unless属性的条件，因此调用连接点方法的返回结果被否决了进入缓存的权利。因此以下两行，每执行一次都会调用一次连接点方法。
		BaseDept dept8 = deptService.findById(9998);
		BaseDept dept9 = deptService.findById(9998);

	}

	@Test
	@Repeat(30)
	@Ignore
	public void test2() {
		taskExecutor.submit(() -> deptService.timeInterval(startTime, endTime));
	}

	@Test
	@Ignore
	public void test3() {
		// DEPT_NO LINE_COUNTS
		// 1 14
		// 2 15
		// 4 25
		// 5 1
		// 9998 3
		// 3 16
		BaseDept dept1_1 = deptService.deptWithAllInfo(1);
		BaseDept dept1_2 = deptService.deptWithAllInfo(1);
		Assert.assertTrue(dept1_1 != dept1_2);

		BaseDept dept2_1 = deptService.deptWithAllInfo(2);
		BaseDept dept2_2 = deptService.deptWithAllInfo(2);
		Assert.assertTrue(dept2_1 != dept2_2);

		BaseDept dept3_1 = deptService.deptWithAllInfo(3);
		BaseDept dept3_2 = deptService.deptWithAllInfo(3);
		Assert.assertTrue(dept3_1 != dept3_2);

		BaseDept dept4_1 = deptService.deptWithAllInfo(4);
		BaseDept dept4_2 = deptService.deptWithAllInfo(4);
		Assert.assertTrue(dept4_1 != dept4_2);

		BaseDept dept5_1 = deptService.deptWithAllInfo(5);
		BaseDept dept5_2 = deptService.deptWithAllInfo(5);
		Assert.assertTrue(dept5_1 == dept5_2);

		BaseDept dept9998_1 = deptService.deptWithAllInfo(9998);
		BaseDept dept9998_2 = deptService.deptWithAllInfo(9998);
		Assert.assertTrue(dept9998_1 == dept9998_2);
	}

	@Test
	@Ignore
	public void test4() {
		BaseDept dept1_1 = deptService.selectDeptWithLines(1);
		BaseDept dept1_2 = deptService.selectDeptWithLines(1);
		Assert.assertTrue(dept1_1 != dept1_2);
		BaseDept dept1_3 = deptService.selectDeptWithLines1(1);
		Assert.assertTrue(dept1_2 == dept1_3);

		BaseDept dept5_1 = deptService.selectDeptWithLines(5);
		BaseDept dept5_2 = deptService.selectDeptWithLines(5);
		Assert.assertTrue(dept5_1 != dept5_2);
		BaseDept dept5_3 = deptService.selectDeptWithLines1(5);
		Assert.assertTrue(dept5_2 != dept5_3);
	}

	@Test
	@Ignore
	public void test5() {
		Future<List<BaseDept>> futureResult = deptService.findByExampleAsync(new BaseDept(deptNo, null));

		boolean success;
		do {
			try {
				// 尝试获取子线程的计算结果5秒钟，此时如果子线程没有执行完，主线程则会阻塞
				List<BaseDept> result = futureResult.get(5, TimeUnit.SECONDS);
				result.forEach(dept -> System.out.println(dept.getDeptName()));
				success = true;
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				success = false;
				// 如果没有获取到子线程的计算结果，主线程就先去干点别的事儿，不用非一直等着子线程的计算结果
				System.out.println("主线程在本次尝试获取子线程结果中没有获取到结果，就做一些其他事情，不用一直等着子线程");
			}
			// 如果没有获取到子线程的计算结果，就再次循环获取
		} while (!success);

		System.out.println("主线程获取到了子线程的计算结果，主线程即将结束");
	}

	@Test
	@Ignore
	public void test6() {
		ListenableFuture<List<BaseDept>> listenableFuture = deptService
				.findByExampleAsyncListenable(new BaseDept(deptNo, null));

		CountDownLatch latch = new CountDownLatch(1);

		/*
		 * 调用ListenableFuture的addCallback方法时的注意：
		 * 
		 * 1.如果在调用addCallback方法时，子线程已经执行完成，那么紧接着会在主线程中执行SuccessCallback对象
		 * 2.如果在调用addCallback方法时，子线程还没有结束，那么主线程继续执行后续的代码，没有阻塞。什么时候子线程执行完成了，
		 * 会将执行SuccessCallback对象的任务提交给线程池，在一个新线程中执行SuccessCallback对象。
		 * 
		 * 注意：即使子线程没有执行完，主线程执行listenableFuture.addCallback这步也不会被阻塞的，会很快就执行后面的代码
		 */
		listenableFuture.addCallback(list -> {
			list.forEach(dept -> System.out.println(dept.getDeptName()));
			latch.countDown();
		}, Throwable::printStackTrace);

		try {
			// 为了不让主线程在子线程执行完之前就结束，因此把主线程阻塞，直到子线程正常执行完成
			latch.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Ignore
	public void test7() {
		BaseDept example = new BaseDept(1177, "1177");
		// 此时肯定返回null，因为返回值类型是Integer，不是Future及子类的
		Integer rows = deptService.insertByExample(example);
		System.out.println(rows);
	}

	@Test
	@Ignore
	public void test8() {
		ListenableFuture<BaseDept> asyncResult = deptService.deptWithAllInfo1(7777);
		CountDownLatch latch = new CountDownLatch(1);

		asyncResult.addCallback(dept -> {
			System.out.println(dept.getDeptName());
			latch.countDown();
		}, Throwable::printStackTrace);

		System.out.println("do something else");

		try {
			System.out.println("lock main thread");
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("end main thread");
	}

	@Test
	@Ignore
	public void test9() {
		Future<BaseDept> future = deptService.selectDeptWithLinesAsync(7777);

		boolean isDone = false;
		do {
			try {
				BaseDept dept = future.get(2, TimeUnit.SECONDS);
				System.out.println(dept.getDeptName());
				isDone = true;
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
			}
			System.out.println("do something else");
		} while (!isDone);
	}

	@Test
	@Ignore
	public void test10() {
		ListenableFuture<BaseDept> asyncResult = deptService.findByIdHibernateAsync(7777);

		CountDownLatch latch = new CountDownLatch(1);
		asyncResult.addCallback(dept -> {
			System.out.println(dept.getDeptName());
			latch.countDown();
		}, Throwable::printStackTrace);

		System.out.println("do something else");

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Ignore
	public void test11() {
		int lineNo = 1234;
		ListenableFuture<BaseLine> asyncResult = lineService.queryByIdAsync(lineNo);

		asyncResult.addCallback(line -> {
			if (Objects.isNull(line)) {
				lineService.saveOrUpdate(new BaseLine(lineNo, String.valueOf(lineNo), lineNo + "线路"));
			}
		}, Throwable::printStackTrace);

		try {
			Thread.sleep(TimeUnit.MINUTES.toMillis(5));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Ignore
	public void test12() throws SQLException {
		lineService.testException1(true);

	}

	@Test
	@Ignore
	public void test13() throws Exception {
		ListenableFuture<Integer> listenableFuture = lineService.testException2(-1);
		listenableFuture.addCallback(System.out::println, throwable -> {
			System.out.println(throwable);
		});

		try {
			Thread.sleep(TimeUnit.SECONDS.toMillis(5));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Ignore
	public void test14() throws Exception {
		Future<Integer> listenableFuture = lineService.testException3(-5);

		try {
			Integer result = listenableFuture.get();
			System.out.println(result);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Test
	public void test15() throws Exception {
		String words = lineService.testException4(null);
		System.out.println(words);
	}
}