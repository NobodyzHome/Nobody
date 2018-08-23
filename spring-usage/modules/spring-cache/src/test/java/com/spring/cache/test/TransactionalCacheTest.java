package com.spring.cache.test;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

import com.spring.cache.definition.TransactionalCacheDefinition;
import com.spring.data.domain.BaseDept;
import com.spring.data.service.BaseDeptService;

public class TransactionalCacheTest {

	private void testContents(BaseDeptService deptService) {
		BaseDept[] result = deptService.queryDept(new BaseDept(1123, null));
		BaseDept[] result1 = deptService.queryDept(new BaseDept(1123, null));

		assertTrue(result != result1);

		BaseDept deptToInsert = new BaseDept();
		deptToInsert.setDeptCode("testDept");
		deptToInsert.setDeptName("测试公司123");
		deptToInsert.setIsRun(false);
		deptToInsert.setModifyDate(new Date());

		deptService.insertDept(deptToInsert);

		BaseDept[] deptSearched1 = deptService.queryDept(deptToInsert);
		BaseDept[] deptSearched2 = deptService.queryDept(deptToInsert);
		assertTrue(deptSearched1 == deptSearched2);

		deptService.updateDept(deptToInsert, deptToInsert);

		BaseDept[] deptSearched3 = deptService.queryDept(deptToInsert);
		BaseDept[] deptSearched4 = deptService.queryDept(deptToInsert);
		assertTrue(deptSearched1 != deptSearched3);
		assertTrue(deptSearched3 == deptSearched4);

		deptService.deleteDept(deptToInsert);

		BaseDept[] deptSearched5 = deptService.queryDept(deptToInsert);
		BaseDept[] deptSearched6 = deptService.queryDept(deptToInsert);

		assertTrue(deptSearched5 != deptSearched6);
		assertTrue(deptSearched3 != deptSearched5);
	}

	@Test
	@Ignore
	public void test1() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"spring/transactionalCache.xml")) {
			BaseDeptService deptService = context.getBean(BaseDeptService.class);
			testContents(deptService);
		}
	}

	@Test
	@Ignore
	public void test2() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				TransactionalCacheDefinition.class)) {
			BaseDeptService deptService = context.getBean(BaseDeptService.class);
			testContents(deptService);
		}
	}

	@Test
	public void test3() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				TransactionalCacheDefinition.class)) {
			BaseDeptService deptService = context.getBean(BaseDeptService.class);

			BaseDept condition = new BaseDept(11, null);
			condition.setIsRun(true);

			BaseDept[] deptSearched1 = deptService.queryDept(condition);
			BaseDept[] deptSearched2 = deptService.queryDept(condition);

			Assert.isTrue(deptSearched1 == deptSearched2, "");
		}
	}
}