package com.spring.testframe;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.ErrorMode;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.test.context.junit4.SpringRunner;

import com.spring.data.domain.BaseDept;
import com.spring.data.service.BaseDeptService;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/test/containers/busness-excercise.xml")
@ActiveProfiles({ "embedded", "deptService" })
@SqlConfig(encoding = "UTF-8", transactionMode = TransactionMode.DEFAULT, errorMode = ErrorMode.CONTINUE_ON_ERROR)
@IfProfileValue(name = "user.name", value = "maziqiang")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS, hierarchyMode = HierarchyMode.CURRENT_LEVEL)
@TestPropertySource(properties = { "clearCacheInterval=5", "offset=100" })
public class TestExcerciseAll1 {

	@Autowired
	private BaseDeptService deptService;

	@Test
	public void test1() {
		BaseDept dept1 = deptService.queryDept(9999);
		BaseDept dept2 = deptService.queryDept(9999);

		// 由于不满足@Cacheable的condition，因此9999参数值的调用结果不会被存入到缓存中
		assertTrue(dept1 != dept2);

		BaseDept dept3 = deptService.queryDept(7777);
		BaseDept dept4 = deptService.queryDept(7777);

		assertTrue(dept3 == dept4);

		List<BaseDept> deptList1 = deptService.queryDepts("%公司%");
		List<BaseDept> deptList2 = deptService.queryDepts("%公司%");

		assertTrue(deptList1 == deptList2);

		List<BaseDept> deptList3 = deptService.queryDepts("%test%");
		List<BaseDept> deptList4 = deptService.queryDepts("%test%");

		// 由于满足了@Cacheable的unless，那么%test%参数值的查询结果被否决了存入到缓存中的资格
		assertTrue(deptList3 != deptList4);
	}

	@Test
	@Ignore
	public void test2() {
		Future<BaseDept> deptFuture = deptService.queryDeptAsync(-1);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BaseDept dept;
		try {
			dept = deptFuture.get();
			System.out.println(dept.getDeptName());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Ignore
	public void test3() {
		deptService.testAsyncException(-1);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
