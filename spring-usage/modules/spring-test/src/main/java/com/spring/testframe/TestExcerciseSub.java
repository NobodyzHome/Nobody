package com.spring.testframe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import com.spring.data.domain.BaseDept;
import com.spring.data.domain.BaseLineTeam;
import com.spring.data.service.BaseDeptService;
import com.spring.data.service.BaseLineTeamService;

@ContextHierarchy(@ContextConfiguration("classpath:spring/test/containers/busness.xml"))
@TestPropertySource(properties = { "zhangsan=张三", "lisi=李四" })
@ActiveProfiles(profiles = "deptService")
public class TestExcerciseSub extends TestExcercise {

	@Autowired
	private BaseDeptService deptService;

	@Autowired
	private BaseLineTeamService lineTeamService;

	@Test
	public void testSub1() {
		List<BaseDept> deptList1 = deptService.queryDepts("%公司%");
		List<BaseDept> deptList2 = deptService.queryDepts("%公司%");

		assertTrue(deptList1 == deptList2);

		deptService.clearCache();

		List<BaseDept> deptList3 = deptService.queryDepts("%公司%");
		List<BaseDept> deptList4 = deptService.queryDepts("%公司%");

		assertFalse(deptList1 == deptList3);
		assertTrue(deptList3 == deptList4);
	}

	@Test
	@DirtiesContext(methodMode = MethodMode.BEFORE_METHOD, hierarchyMode = HierarchyMode.CURRENT_LEVEL)
	public void testSub2() {
		BaseDept dept1 = deptService.queryDept(1);
		BaseDept dept2 = deptService.queryDept(1);

		assertTrue(dept1 == dept2);

		deptService.clearCache();

		deptService.queryDepts("%公司%");
		BaseDept dept3 = deptService.queryDept(1);
		BaseDept dept4 = deptService.queryDept(1);

		assertTrue(dept3 == dept4);
	}

	// @Repeat和@Timed可以联合使用，判断重复执行多次@Test方法所用的时间是否超过了@Timed所限定的时间
	@Test
	// 判断执行完4次的@Test方法所用时间是否超过5000毫秒，如果超过，则认为当前@Test方法执行失败
	@Timed(millis = 5000)
	// @Repeat指定执行4次@Test方法
	@Repeat(4)
	public void testSub3() throws InterruptedException {
		// 每次执行@Test方法，都会使当前线程阻塞1000毫秒
		Thread.sleep(1000);
	}

	@Test
	@Timed(millis = 3000)
	/*
	 * 注意：如果在一个@Test方法上只有@Sql注解，没有@Transactional注解，那么spring会在执行@
	 * Sql脚本时新建一个数据库连接来执行@Sql指定的脚本，执行完成后即会提交事务，所以执行到@Test方法时，执行@Sql脚本的事务就已经提交了。
	 * 有点类似于设置了@SqlConfig(transactionMode=ISOLATED)。
	 * 
	 * 而如果在@Test方法上设置了@Transactional注解，那么spring默认会使用为该@Test方法所开启的事务来执行@Sql脚本，
	 * 当然前提是@Sql没有配置@SqlConfig(transactionMode=ISOLATED)。
	 */
	@Transactional
	@Sql(statements = { "insert into ext_base_line_team values(7777,'路队7777','101路,20路,88路,99路,921路',8888)",
			"insert into ext_base_line_team values(6666,'路队6666','3路,7路,999路,831路,898路',7777)" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
	public void testSub4() {
		/*
		 * 由于设置了异步调用，因此实际是在新线程里调用queryAllAsync方法，因此在新线程里又会创建一个数据库连接和事务。由于@
		 * Test方法所在的线程所开启的事务，也就是执行了@Sql脚本的事务并没有提交。因此queryAllAsync方法所对应的事务里看不到@
		 * Test方法里所插入的ext_base_line_team的数据。
		 */
		Future<List<BaseLineTeam>> lineTeamFuturnResult = lineTeamService.queryAllAsync();
		try {
			List<BaseLineTeam> lineTeamList = lineTeamFuturnResult.get(3, TimeUnit.SECONDS);
			lineTeamList
					.forEach(lineTeam -> System.out.println(lineTeam.getGroupName() + ":" + lineTeam.getDescription()));
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
		}
	}
}
