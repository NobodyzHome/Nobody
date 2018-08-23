package com.spring.cache.test;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.spring.cache.composite.CompositeCacheService;
import com.spring.cache.definition.CompositeCacheDefinition;
import com.spring.data.domain.BaseDept;

public class CompositeCacheTest {
	@SuppressWarnings("unused")
	@Test
	@Ignore
	public void test1() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"spring/cacheUsingComposite.xml")) {
			CompositeCacheService cacheService = context.getBean(CompositeCacheService.class);
			CacheManager jdkCacheManager = context.getBean(ConcurrentMapCacheManager.class);

			testContents(cacheService);
		}
	}

	@Test
	public void test2() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				CompositeCacheDefinition.class)) {
			CompositeCacheService cacheService = context.getBean(CompositeCacheService.class);

			testContents(cacheService);
		}
	}

	private void testContents(CompositeCacheService cacheService) {
		BaseDept deptSearched1 = cacheService.multipleCacheable(11, "testDept", "测试公司");
		BaseDept deptSearched2 = cacheService.cacheable1(11, "testDept", "测试公司");
		BaseDept deptSearched3 = cacheService.cacheable2(11, "testDept", "测试公司");
		BaseDept deptSearched4 = cacheService.cacheable3(11, "testDept", "测试公司");

		assertTrue(deptSearched1 == deptSearched2);
		assertTrue(deptSearched2 == deptSearched3);
		assertTrue(deptSearched3 == deptSearched4);

		BaseDept deptSearched5 = cacheService.multiplePut(11, "testDept", "测试公司");
		BaseDept deptSearched6 = cacheService.cacheable1(11, "testDept", "测试公司");
		BaseDept deptSearched7 = cacheService.cacheable2(11, "testDept", "测试公司");
		BaseDept deptSearched8 = cacheService.cacheable3(11, "testDept", "测试公司");

		assertTrue(deptSearched5 == deptSearched6);
		assertTrue(deptSearched6 == deptSearched7);
		assertTrue(deptSearched7 == deptSearched8);
		assertTrue(deptSearched1 != deptSearched5);

		// 调用noOperCache方法，由于该方法的缓存操作对应的是NoCache对象，由于该对象的get方法永远返回null，因此调用该方法就一直会直接调用目标方法
		BaseDept deptSearched9 = cacheService.noOperCache(11, "testDept", "测试公司");
		BaseDept deptSearched10 = cacheService.noOperCache(11, "testDept", "测试公司");
		BaseDept deptSearched11 = cacheService.noOperCache(11, "testDept", "测试公司");

		assertTrue(deptSearched9 != deptSearched10);
		assertTrue(deptSearched10 != deptSearched11);

		BaseDept deptSearched12 = cacheService.unlessCacheable(1000, "deptTest", "测试测试");
		BaseDept deptSearched13 = cacheService.unlessCacheable(1000, "deptTest", "测试测试");

		// 满足condition=true，那么会执行@CachePut缓存操作。同时满足unless=false，那么该缓存操作会将调用连接点方法的返回值记录到Cache中。
		BaseDept deptSearched14 = cacheService.resultPut(3000, "deptTest", "测试测试");
		BaseDept deptSearched15 = cacheService.cacheable4(3000, "deptTest", "测试测试");
		assertTrue(deptSearched14 == deptSearched15);

		// 不满足condition=true，因此不会执行@CachePut缓存操作。因此，调用连接点方法的返回结果不会记录到Cache中。
		BaseDept deptSearched16 = cacheService.resultPut(1000, "deptTest", "测试测试");
		BaseDept deptSearched17 = cacheService.cacheable2(1000, "deptTest", "测试测试");
		assertTrue(deptSearched16 != deptSearched17);

		// 满足condition=true，因此会执行@CachePut缓存操作。但由于也满足了unless=true，那么否决了将该连接点返回结果记录到Cache中
		BaseDept deptSearched18 = cacheService.resultPut(2000, "deptTest", "测试测试");
		BaseDept deptSearched19 = cacheService.cacheable2(2000, "deptTest", "测试测试");
		assertTrue(deptSearched18 != deptSearched19);

		BaseDept deptSearched20 = cacheService.resultPut(3000, "deptLaYa", "拉娅公司");
		BaseDept deptSearched23 = cacheService.cacheable4(3000, "deptLaYa", "拉娅公司");
		cacheService.resultEvict(3000, "deptLaYa", "拉娅公司");
		BaseDept deptSearched22 = cacheService.cacheable4(3000, "deptLaYa", "拉娅公司");
		assertTrue(deptSearched20 != deptSearched22);
		assertTrue(deptSearched20 == deptSearched23);
	}
}
