package com.spring.cache.test;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import com.spring.cache.composite.CompositeCacheService;
import com.spring.cache.definition.EhCacheDefinition;
import com.spring.cache.ehcache.EhCacheService;
import com.spring.data.domain.BaseDept;

public class EhCacheTest {

	@Test
	@Ignore
	public void test1() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext()) {
			ConfigurableEnvironment environment = context.getEnvironment();
			environment.addActiveProfile("annotationCacheConfig");
			context.setConfigLocation("spring/cacheUsingEhcache.xml");
			context.refresh();

			EhCacheService cacheService = context.getBean(EhCacheService.class);

			BaseDept condition1 = new BaseDept();
			condition1.setDeptNo(9999);
			condition1.setDeptName("测试公司");
			condition1.setDeptCode("testDept");

			BaseDept condition2 = new BaseDept();
			condition2.setDeptNo(9999);
			condition2.setDeptName("测试公司");
			condition2.setDeptCode("testDept");

			Date modifyDate = new Date();

			BaseDept deptSearched1 = cacheService.cacheableQuery1(condition1, modifyDate);
			BaseDept deptSearched2 = cacheService.cacheableQuery1(condition2, modifyDate);
			assertTrue(deptSearched1 == deptSearched2);

			BaseDept condition3 = new BaseDept(8888, "testHAHA");
			condition3.setDeptName("哈哈公司");

			BaseDept deptSearched3 = cacheService.multipleCacheableQuery(condition3, modifyDate);
			BaseDept deptSearched4 = cacheService.cacheableQuery1(condition3, modifyDate);
			BaseDept deptSearched5 = cacheService.cacheableQuery2(condition3, modifyDate);
			BaseDept deptSearched6 = cacheService.cacheableQuery3(condition3, modifyDate);

			assertTrue(deptSearched3 == deptSearched4);
			assertTrue(deptSearched4 == deptSearched5);
			assertTrue(deptSearched5 == deptSearched6);

			BaseDept deptSearched7 = cacheService.multipulePutQuery(condition3, modifyDate);
			BaseDept deptSearched8 = cacheService.cacheableQuery1(condition3, modifyDate);
			BaseDept deptSearched9 = cacheService.cacheableQuery2(condition3, modifyDate);
			BaseDept deptSearched10 = cacheService.cacheableQuery3(condition3, modifyDate);

			assertTrue(deptSearched7 == deptSearched8);
			assertTrue(deptSearched8 == deptSearched9);
			assertTrue(deptSearched9 == deptSearched10);
			assertTrue(deptSearched3 != deptSearched7);
		}
	}

	@Test
	@Ignore
	public void test2() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				EhCacheDefinition.class)) {
			BaseDept condition1 = new BaseDept();
			condition1.setDeptNo(9999);
			condition1.setDeptName("测试公司");
			condition1.setDeptCode("testDept");

			Date modifyDate = new Date();

			EhCacheService cacheService = context.getBean(EhCacheService.class);
			BaseDept deptSearched1 = cacheService.multipleCacheableQuery(condition1, modifyDate);
			BaseDept deptSearched2 = cacheService.cacheableQuery1(condition1, modifyDate);
			BaseDept deptSearched3 = cacheService.cacheableQuery2(condition1, modifyDate);
			BaseDept deptSearched4 = cacheService.cacheableQuery3(condition1, modifyDate);

			assertTrue(deptSearched1 == deptSearched2);
			assertTrue(deptSearched2 == deptSearched3);
			assertTrue(deptSearched3 == deptSearched4);

			BaseDept deptSearched5 = cacheService.multipulePutQuery(condition1, modifyDate);
			BaseDept deptSearched6 = cacheService.cacheableQuery1(condition1, modifyDate);
			BaseDept deptSearched7 = cacheService.cacheableQuery2(condition1, modifyDate);
			BaseDept deptSearched8 = cacheService.cacheableQuery3(condition1, modifyDate);

			assertTrue(deptSearched5 == deptSearched6);
			assertTrue(deptSearched6 == deptSearched7);
			assertTrue(deptSearched7 == deptSearched8);
			assertTrue(deptSearched5 != deptSearched4);
		}
	}

}