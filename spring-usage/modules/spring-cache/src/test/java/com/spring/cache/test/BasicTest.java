package com.spring.cache.test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

import com.spring.cache.definition.CacheDefinition;
import com.spring.cache.jdk.CacheResolverCacheService;
import com.spring.cache.jdk.ConcurrentMapCacheAnnotationService;
import com.spring.cache.jdk.ConcurrentMapCacheService;
import com.spring.data.domain.BaseDept;
import com.spring.data.service.BaseDeptService;

public class BasicTest {

	@Test
	public void test1() {
		try (ClassPathXmlApplicationContext cacheContext = new ClassPathXmlApplicationContext()) {
			cacheContext.getEnvironment().addActiveProfile("test");
			cacheContext.setConfigLocation("spring/cache.xml");
			cacheContext.refresh();

			BaseDeptService deptService = cacheContext.getBean(BaseDeptService.class);

			BaseDept deptToQuery = new BaseDept(1, null);
			deptService.queryDept(deptToQuery);
			deptService.queryDept(deptToQuery);

			BaseDept deptToInsert = new BaseDept();
			deptToInsert.setDeptCode("testDept");
			deptToInsert.setDeptName("测试123公司");
			deptToInsert.setModifyDate(new Date());
			deptToInsert.setIsRun(true);

			deptService.insertDept(deptToInsert);

			deptService.queryDept(deptToQuery);
			deptService.queryDept(deptToQuery);
			deptService.queryDept(deptToInsert);
			deptService.queryDept(deptToInsert);

			deptService.deleteDept(deptToInsert);

			deptService.queryDept(deptToQuery);
			deptService.queryDept(deptToQuery);
			deptService.queryDept(deptToInsert);
			deptService.queryDept(deptToInsert);
		}
	}

	@Test
	@Ignore
	public void test2() {
		try (ClassPathXmlApplicationContext cacheContext = new ClassPathXmlApplicationContext();
				ClassPathXmlApplicationContext dataAccessContext = new ClassPathXmlApplicationContext()) {
			cacheContext.setConfigLocation("spring/cache.xml");
			dataAccessContext.setConfigLocation("classpath*:spring/data-access.xml");
			cacheContext.setParent(dataAccessContext);

			// 注意：需要先刷新父容器，才能刷新子容器
			dataAccessContext.refresh();
			cacheContext.refresh();

			BaseDeptService deptService = cacheContext.getBean(BaseDeptService.class);
			BaseDept condition = new BaseDept(11, null);

			deptService.queryDept(condition);
			deptService.queryDept(condition);
		}
	}

	@Test
	@Ignore
	public void test3() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/cacheUsingJdk.xml")) {
			ConcurrentMapCacheService cacheService = context.getBean(ConcurrentMapCacheService.class);

			BaseDept dept1 = context.getBean(BaseDept.class);

			BaseDept dept2 = cacheService.cacheAbleService(dept1, "yoyo", DateUtils.addYears(new Date(), 1));
			BaseDept dept2_1 = cacheService.cacheAbleService(dept1, null, null);
			Assert.isTrue(dept2 == dept2_1, "应该为true，因为是从缓存中获取的数据");

			BaseDept dept3 = context.getBean(BaseDept.class);
			dept3.setDeptNo(9999);

			BaseDept dept4 = cacheService.cacheAbleService(dept3, "haha", null);
			BaseDept dept4_1 = cacheService.cacheAbleService(dept3, "haha", null);
			Assert.isTrue(dept4 != dept4_1, "由于设置了unless，因此deptNo为9999的数据并没有被缓存");

			cacheService.clearSpecificCache(dept1.getDeptNo());
			BaseDept dept5 = cacheService.cacheAbleService(dept1, null, null);
			BaseDept dept5_1 = cacheService.cacheAbleService(dept1, null, null);
			Assert.isTrue(dept5 == dept5_1, "");
			Assert.isTrue(dept2 != dept5, "由于缓存中deptNo=11的数据已经被清除了，因此是重新调用连接点，获取新的缓存对象");

			BaseDept dept6 = context.getBean(BaseDept.class);
			BaseDept dept7 = cacheService.cacheAbleService(dept6, null, null);
			BaseDept dept7_1 = cacheService.cacheAbleService(dept6, null, null);
			Assert.isTrue(dept7 == dept7_1, "");

			cacheService.clearAllCache();

			BaseDept dept8 = cacheService.cacheAbleService(dept5, null, null);
			BaseDept dept8_1 = cacheService.cacheAbleService(dept5, null, null);
			Assert.isTrue(dept8 == dept8_1, "");
			Assert.isTrue(dept8 != dept5, "");

			BaseDept dept9 = cacheService.cacheAbleService(dept6, null, null);
			BaseDept dept9_1 = cacheService.cacheAbleService(dept6, null, null);
			Assert.isTrue(dept9 == dept9_1, "");
			Assert.isTrue(dept9 != dept6, "");

			BaseDept dept10 = context.getBean(BaseDept.class);
			BaseDept dept10_1 = cacheService.cacheAbleService(dept10, null, null);
			BaseDept dept10_2 = cacheService.cacheAbleService(dept10, null, null);
			Assert.isTrue(dept10_1 == dept10_2, "");

			BaseDept dept11 = cacheService.forceUpdateCache(dept10.getDeptNo(), dept10.getDeptCode(),
					dept10.getDeptName(), dept10.getModifyDate(), dept10.getIsRun());
			BaseDept dept11_1 = cacheService.cacheAbleService(dept11, null, null);
			BaseDept dept11_2 = cacheService.cacheAbleService(dept11, null, null);
			Assert.isTrue(dept11_1 == dept11_2, "");

			BaseDept dept12 = cacheService.forceUpdateCache(dept10.getDeptNo(), dept10.getDeptCode(),
					dept10.getDeptName(), dept10.getModifyDate(), dept10.getIsRun());
			BaseDept dept12_1 = cacheService.cacheAbleService(dept12, null, null);
			BaseDept dept12_2 = cacheService.cacheAbleService(dept12, null, null);
			Assert.isTrue(dept12_1 == dept12_2, "");
			// 由于调用CachePut是每次都会调用连接点并保存缓存数据，因此以下获得的是不同的对象
			Assert.isTrue(dept11 != dept12, "");

			BaseDept dept13 = cacheService.clearThenPutCache(dept12);
			BaseDept dept13_1 = cacheService.cacheAbleService(dept13, null, null);
			BaseDept dept13_2 = cacheService.cacheAbleService(dept13, null, null);

			Assert.isTrue(dept13 != dept12, "");
			Assert.isTrue(dept13_1 == dept13_2, "");

			Date date = new Date();
			String formatted1 = cacheService.cacheAbleService2(date);
			String formatted1_1 = cacheService.cacheAbleService2(date);
			Assert.isTrue(formatted1 == formatted1_1, "");
		}
	}

	@Test
	@Ignore
	public void test4() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/cacheUsingJdk.xml")) {
			ConcurrentMapCacheService cacheService = context.getBean(ConcurrentMapCacheService.class);
			cacheService.clearSpecificCache(90);

			Date date = DateUtils.addDays(new Date(), 2);
			cacheService.cacheAbleService2(date);
		}
	}

	@Test
	@Ignore
	public void test5() throws ParseException {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"spring/annotationCacheUsingJdk.xml")) {
			ConcurrentMapCacheAnnotationService cacheService = context
					.getBean(ConcurrentMapCacheAnnotationService.class);

			Date date1 = DateUtils.truncate(new Date(), Calendar.SECOND);
			// Date date1 = new Date();
			String formatted1_1 = cacheService.cacheableService1(date1);
			String formatted1_2 = cacheService.cacheableService1(date1);
			Assert.isTrue(formatted1_1 == formatted1_2, "");

			Date date1_1 = cacheService.cacheableService2(formatted1_1);
			Date date1_2 = cacheService.cacheableService2(formatted1_2);
			Assert.isTrue(date1_1 == date1_2, "");

			cacheService.evictSingleCache(date1_1);

			String formatted2_1 = cacheService.cacheableService1(date1);
			String formatted2_2 = cacheService.cacheableService1(date1_2);

			Assert.isTrue(formatted2_1 == formatted2_2, "");

			Date date2 = DateUtils.addYears(date1, -1);
			// 由于传入的date2的值不满足CachePut中的condition条件，因此，实际上没有将调用目标对象的返回结果存储在缓存中
			String date2_1 = cacheService.forceUpdateCache(date2);
			// 由于forceUpdateCache没有将返回结果记录到缓存中，因此下面方法则会再调用目标对象，然后将返回结果存储在缓存中
			String date2_2 = cacheService.cacheableService1(date2);
			String date2_3 = cacheService.cacheableService1(date2);

			Assert.isTrue(date2_1 != date2_2, "");
			Assert.isTrue(date2_2 == date2_3, "");

			Date date3 = DateUtils.addYears(date1, 1);
			String date3_1 = cacheService.cacheableService1(date3);
			String date3_2 = cacheService.cacheableService1(date3);
			Assert.isTrue(date3_1 == date3_2, "");

			// 由于是CachePut操作，因此每次调用forceUpdateCache方法，都会调用目标对象，并将目标对象的返回结果强制更新到缓存中
			String date3_3 = cacheService.forceUpdateCache(date3);
			String date3_4 = cacheService.forceUpdateCache(date3);
			// 由于上面是掉了两次目标对象，因此两次返回结果是不同的对象
			Assert.isTrue(date3_3 != date3_4, "");

			// 由于是CacheAble操作，因此以下都是从缓存中获取对象
			String date3_5 = cacheService.cacheableService1(date3);
			String date3_6 = cacheService.cacheableService1(date3);
			Assert.isTrue(date3_4 == date3_5, "");
			Assert.isTrue(date3_5 == date3_6, "");

			Date date4 = cacheService.cacheableService2("2017-11-20 13:30:22");
			String date4_1 = cacheService.cacheableService1(date4);
			String date4_2 = cacheService.cacheableService1(date4);
			/*
			 * 由于CachePut操作设定的unless属性为#date.month==10，因此，对于传入的参数为11月的Date对象，
			 * 虽然会调用目标对象，但不会将目标对象的返回值存储到缓存中。因此缓存中对应key的value没有得到更新
			 */
			String date4_3 = cacheService.forceUpdateCache(date4);
			String date4_4 = cacheService.cacheableService1(date4);

			Assert.isTrue(date4_1 == date4_2, "");
			Assert.isTrue(date4_2 != date4_3, "");
			Assert.isTrue(date4_2 == date4_4, "");

			Date date5 = new Date();
			String date5_1 = cacheService.cacheableService1(date5);
			String date5_2 = cacheService.cacheableService1(date5);
			// 先清除缓存中的该key对应的value，再存放到缓存中。因此该方法相当于@CachePut操作。
			String date5_3 = cacheService.evictThenPut(date5);
			// date5_4、date5_5获取的就都是evictThenPut方法向缓存重新存放的数据
			String date5_4 = cacheService.cacheableService1(date5);
			String date5_5 = cacheService.cacheableService1(date5);

			Assert.isTrue(date5_1 == date5_2, "");
			Assert.isTrue(date5_2 != date5_3, "");
			Assert.isTrue(date5_3 == date5_4, "");
			Assert.isTrue(date5_4 == date5_5, "");
			Assert.isTrue(date5_2 != date5_5, "");

		}
	}

	@Test
	@Ignore
	public void test6() throws ParseException {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				CacheDefinition.class)) {
			ConcurrentMapCacheAnnotationService cacheService1 = context
					.getBean(ConcurrentMapCacheAnnotationService.class);
			CacheResolverCacheService cacheService2 = context.getBean(CacheResolverCacheService.class);

			Date date1 = new Date();
			String formatted1_1 = cacheService1.cacheableService1(date1);
			String formatted1_2 = cacheService1.cacheableService1(date1);
			Assert.isTrue(formatted1_1 == formatted1_2, "");

			String formatted1_3 = cacheService2.cacheableService(date1);
			String formatted1_4 = cacheService2.cacheableService(date1);
			Assert.isTrue(formatted1_3 == formatted1_4, "");
			// Assert.isTrue(formatted1_2 != formatted1_4, null);

			Date date1_1 = cacheService1.cacheableService2(formatted1_4);
			Date date1_2 = cacheService2.cacheableService2(formatted1_3);
			Assert.isTrue(date1_1 == date1_2, "");

			Date date2 = new Date();

			// cacheService2.cacheableService会调用目标方法，然后将目标方法的返回结果存储的date_region和dept_region这两个Cache中
			String formatted2_1 = cacheService2.cacheableService(date2);

			/*
			 * cacheService1.cacheableService3会尝试从dept_region这个Cache中获取数据，
			 * 由于cacheService2.cacheableService已经向该Cache写入了对应的数据，
			 * 因此直接从Cache中获取即可。
			 */
			String formatted2_2 = cacheService1.cacheableService3(date2);
			Assert.isTrue(formatted2_1 == formatted2_2, "");

			// 尝试向date_region和dept_region这两个缓存中获取数据，如果不能获取，则调用目标方法，并将目标方法的返回结果存储至缓存中
			String formatted2_7 = cacheService2.cacheableService(date2);
			String formatted2_8 = cacheService2.cacheableService(date2);
			String formatted2_9 = cacheService2.cacheableService(date2);
			Assert.isTrue(formatted2_7 == formatted2_8, "");
			Assert.isTrue(formatted2_9 == formatted2_8, "");

			// 不管缓存中是否有该key对应的value，强制将目标对象的返回结果更新至date_region和dept_region中
			String formatted2_3 = cacheService2.forceUpdateCache(date2);
			Assert.isTrue(formatted2_3 != formatted2_7, "");

			// 从date_region或dept_region中获取对应的value
			String formatted2_4 = cacheService2.cacheableService(date2);

			// 从date_region中获取对应的value
			String formatted2_5 = cacheService1.cacheableService1(date2);

			// 从dept_region中获取对应的value
			String formatted2_6 = cacheService1.cacheableService3(date2);

			// 因此从date_region或dept_region中获取的date2对应的value，应该是相同的对象。
			Assert.isTrue(formatted2_3 != formatted2_2, "");
			Assert.isTrue(formatted2_3 == formatted2_4, "");
			Assert.isTrue(formatted2_3 == formatted2_5, "");
			Assert.isTrue(formatted2_3 == formatted2_6, "");

			// 该方法会把date_region或dept_region中date2对应的value都清除
			cacheService2.evictSpecificKey(date2);

			// 从date_region中获取date2对应的value，由于已经被清除，因此重新调用目标方法，将目标方法的返回结果记录到date_region中
			String formatted2_10 = cacheService1.cacheableService1(date2);

			// 从dept_region中获取date2对应的value，由于已经被清除，因此重新调用目标方法，将目标方法的返回结果记录到dept_region中
			String formatted2_11 = cacheService1.cacheableService3(date2);
			Assert.isTrue(formatted2_10 != formatted2_11, "");

			Date date3 = new Date();

			// 向date_region和dept_region中存储date3对应的key和value
			String formatted3_1 = cacheService2.cacheableService(date3);
			// 传入的是date2,但由于该方法对应的@CacheEvict的allEntries为true，因此会清除每一个Cache中所有的key和value
			cacheService2.evictAllKey(date2);
			// 向date_region和dept_region中存储date3对应的key和value
			String formatted3_2 = cacheService2.cacheableService(date3);
			// 从date_region获取date3对应的key和value
			String formatted3_3 = cacheService1.cacheableService1(date3);
			// 从dept_region获取date3对应的key和value
			String formatted3_4 = cacheService1.cacheableService3(date3);
			Assert.isTrue(formatted3_1 != formatted3_2, "");
			Assert.isTrue(formatted3_2 == formatted3_3, "");
			Assert.isTrue(formatted3_2 == formatted3_4, "");
		}
	}

	@Test
	@Ignore
	public void test7() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				CacheDefinition.class)) {
			ConcurrentMapCacheAnnotationService cacheService1 = context
					.getBean(ConcurrentMapCacheAnnotationService.class);

			ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
			threadPool.setCorePoolSize(10);
			threadPool.initialize();

			Date date1 = new Date();
			Date date2 = DateUtils.addYears(new Date(), 1);

			CountDownLatch countDown = new CountDownLatch(6);

			Runnable mission1 = () -> {
				cacheService1.cacheableService4(date1);
				countDown.countDown();
			};

			Runnable mission2 = () -> {
				cacheService1.cacheableService4(date2);
				countDown.countDown();
			};

			threadPool.execute(mission1);
			threadPool.execute(mission1);
			threadPool.execute(mission1);
			threadPool.execute(mission2);
			threadPool.execute(mission2);
			threadPool.execute(mission2);

			try {
				countDown.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	@Ignore
	public void test8() throws ParseException {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				CacheDefinition.class)) {
			CacheResolverCacheService cacheService1 = context.getBean(CacheResolverCacheService.class);

			/*
			 * 这两个Date对象虽然不是同一个Date对象，但是时间相同，满足Date类对equals和hashcode的覆盖，即date1.
			 * equals(date2)&&date1.hashcode()=date2.hashcode返回true
			 */
			Date date1 = new Date();
			Date date2 = new Date(date1.getTime());

			String formatted1 = cacheService1.cacheableService(date1);

			/*
			 * 使用date2这个对象来和缓存中的key对比，由于date2和在Cache中的date1满足date1.equals(date2)
			 * &&date1.hashcode()=date2.hashcode返回true，
			 * 因此会从Cache中获取key为date1对象的value
			 */
			String formatted2 = cacheService1.cacheableService(date2);
			Assert.isTrue(formatted1 == formatted2, "");

			Date date3 = cacheService1.cacheableService2("2017-12-01 11:33:22");
			/*
			 * 当调用cacheableService2方法，@Cacheable的unless属性的spel表达式的值判断为true时，
			 * 不会从Cache中获取数据，而是直接调用责任链，进而调用目标方法。因此，以下三行都是目标方法返回的结果。因此当然是不同的对象。
			 */
			String formatted3 = cacheService1.cacheableServiceUnless(date3);
			String formatted4 = cacheService1.cacheableServiceUnless(date3);
			String formatted5 = cacheService1.cacheableServiceUnless(date3);
			Assert.isTrue((formatted3 != formatted4) == (formatted5 != formatted4), "");
		}
	}

	@Test
	@Ignore
	public void test9() throws ParseException {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				CacheDefinition.class)) {
			ConcurrentMapCacheAnnotationService cacheService = context
					.getBean(ConcurrentMapCacheAnnotationService.class);

			Date date1 = new Date();
			Date date2 = new Date(date1.getTime());

			String formatted1 = cacheService.cacheableService1(date1);
			String formatted2 = cacheService.cacheableService3(date2);
			String formatted3 = cacheService.cachingService1(date2);
			Assert.isTrue(formatted1 != formatted2, "");
			Assert.isTrue(formatted2 == formatted3, "");

			String formatted4 = cacheService.cacheableService3(date2);
			Assert.isTrue(formatted3 != formatted4, "");

			String formatted5 = cacheService.cacheableService5(date2);
			Assert.isTrue(formatted3 != formatted5, "");

			Date date3 = new Date();
			String formatted6 = cacheService.cachingService1(date3);
			String formatted7 = cacheService.cacheableService1(date3);
			String formatted8 = cacheService.cacheableService3(date3);
			String formatted9 = cacheService.cacheableService5(date3);

			Assert.isTrue(formatted6 == formatted7, "");
			Assert.isTrue(formatted6 == formatted9, "");
			Assert.isTrue(formatted6 != formatted8, "");

			Date date4 = cacheService.cacheableService2("2016-10-10 09:55:51");
			String formatted10 = cacheService.cachingService2(date4);
			String formatted11 = cacheService.cacheableService1(date4);
			String formatted12 = cacheService.cacheableService3(date4);
			String formatted13 = cacheService.cacheableService5(date4);

			Assert.isTrue(formatted10 == formatted11, "");
			Assert.isTrue(formatted11 == formatted12, "");
			Assert.isTrue(formatted12 == formatted13, "");

			Date date5 = cacheService.cacheableService2("2016-10-10 09:55:51");
			String formatted14 = cacheService.cachingService3(date5);
			String formatted15 = cacheService.cachingService3(date5);
			String formatted16 = cacheService.cacheableService1(date5);
			String formatted17 = cacheService.cacheableService3(date5);
			String formatted18 = cacheService.cacheableService5(date5);

			Assert.isTrue(formatted14 != formatted15, "");
			Assert.isTrue(formatted15 == formatted16, "");
			Assert.isTrue(formatted15 == formatted17, "");
			Assert.isTrue(formatted15 == formatted18, "");

			Date date6 = new Date();
			String formatted19 = cacheService.cacheableService6(date6);
			String formatted20 = cacheService.cacheableService1(date6);
			String formatted21 = cacheService.cacheableService3(date6);
			String formatted22 = cacheService.cacheableService5(date6);
			
			Assert.isTrue(formatted19 == formatted20, "");
			Assert.isTrue(formatted20 == formatted21, "");
			Assert.isTrue(formatted21 != formatted22, "");
		}
	}
}