package com.spring.testframe;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.ErrorMode;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.spring.data.dao.BaseDeptMapper;
import com.spring.data.dao.BaseLineTeamMapper;
import com.spring.data.domain.BaseDept;
import com.spring.data.domain.BaseLineTeam;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/test/containers/data.xml")
@ActiveProfiles("test")
@ProfileValueSourceConfiguration
/*
 * 注意：默认情况下，spring是在@Test方法所开启的事务中执行@Sql脚本的，但我们也可以设置transactionMode为ISOLATED，
 * 那么会在一个新的事务里执行@Sql脚本，并且在@Sql脚本执行完后会立即提交这个新事务，不论@Test上是@Commit还是@Rollback。
 * 实际上，@Test上的@Commit或@Rollback控制的是@Test方法执行完成后，是进行事务提交还是回滚。
 * 
 * Indicates that SQL scripts should always be executed in a new, isolated
 * transaction that will be immediately committed.
 */
@SqlConfig(encoding = "UTF-8", transactionMode = TransactionMode.DEFAULT, errorMode = ErrorMode.FAIL_ON_ERROR)
@TestPropertySource(locations = { "classpath:database/datasource.cfg.properties",
		"classpath:database/jiayuguan.properties" }, inheritLocations = true, properties = { "hello=你好",
				"world=世界" }, inheritProperties = true)
public class TestExcercise {
	@Autowired
	private BaseDeptMapper deptMapper;

	@Autowired
	private BaseLineTeamMapper lineTeamMapper;

	@Value("【#{random}】${hello} and ${world} and ${db.jiayuguan.url}")
	private String contents;

	@Test
	@Sql(scripts = "classpath:sql/dept.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, config = @SqlConfig(errorMode = ErrorMode.CONTINUE_ON_ERROR))
	@Transactional
	public void test1() {
		List<BaseDept> deptList = deptMapper.queryDepts("%公司%");
		deptList.forEach(dept -> System.out.println(dept.getDeptName()));
		System.out.println("TestExcercise.test1():" + contents);
	}

	@Test
	public void test2() {
		System.out.println("TestExcercise.test2():" + contents);
	}

	@Test
	@DirtiesContext(methodMode = MethodMode.BEFORE_METHOD, hierarchyMode = HierarchyMode.EXHAUSTIVE)
	public void test3() {
		System.out.println("TestExcercise.test3():" + contents);
	}

	@BeforeTransaction
	public void beforeTransaction() {
		System.out.println("TestExcercise.beforeTransaction()");
	}

	@AfterTransaction
	public void afterTransaction() {
		System.out.println("TestExcercise.afterTransaction()");
	}

	@BeforeTransaction
	public void beforeTx1() {
		System.out.println("TestExcercise.beforeTx1()");
	}

	@Test
	@Repeat(3)
	public void test4() {
		System.out.println("TestExcercise.test4():" + contents);
	}

	@Test
	@IfProfileValue(name = "user.name", values = { "hewenbin", "maziqiang123", "zhangsan" })
	@Transactional
	@Sql(statements = { "insert into base_dept values(9999,'9999','测试公司1',6,11,systimestamp,1)",
			"insert into base_dept values(8888,'8888','测试公司2',6,11,systimestamp,0)",
			"insert into base_dept values(7777,'7777','测试公司3',6,11,systimestamp,1)" })
	@Sql(statements = { "insert into ext_base_line_team values(9999,'路队9999','1,2,3,4,5',7777)",
			"insert into ext_base_line_team values(8888,'路队8888','9,10,11,12,13',8888)" })
	@Commit
	public void test5() {
		List<BaseDept> deptList = deptMapper.queryDepts("%公司%");
		List<BaseLineTeam> lineTeamList = lineTeamMapper.queryAll();

		deptList.forEach(dept -> System.out.println(dept.getDeptName()));
		lineTeamList.forEach(lineTeam -> System.out.println(
				lineTeam.getGroupName() + ":" + (lineTeam.getDept() == null ? "无" : lineTeam.getDept().getDeptName())));
	}

	@Test
	@Ignore
	@Sql(statements = { "insert into base_dept values(9999,'9999','测试公司1',6,11,systimestamp,1)",
			"insert into base_dept values(8888,'8888','测试公司2',6,11,systimestamp,0)",
			"insert into base_dept values(7777,'7777','测试公司3',6,11,systimestamp,1)" }, config = @SqlConfig(transactionMode = TransactionMode.ISOLATED), executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
	@Transactional
	@Rollback
	public void test6() {
		List<BaseDept> deptList = deptMapper.queryDepts("%公司%");

		deptList.forEach(dept -> System.out.println(dept.getDeptName()));
	}

}