package com.spring.testframe;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
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

import com.spring.data.domain.BaseLineTeam;
import com.spring.data.service.BaseLineTeamService;

@RunWith(SpringRunner.class)
@ContextConfiguration({ "classpath:spring/test/containers/data.xml", "classpath:spring/test/containers/busness.xml" })
@ActiveProfiles("test")
@TestPropertySource(properties = { "hello=你好", "world=世界", "fixedDelay=3000",
		"clearCacheOffset=1000" }, inheritProperties = true)
@SqlConfig(encoding = "UTF-8", transactionMode = TransactionMode.INFERRED, errorMode = ErrorMode.CONTINUE_ON_ERROR)
@IfProfileValue(name = "user.name", value = "maziqiang")
public class TestParentCase {

	@Autowired
	private BaseLineTeamService lineTeamService;

	@Value("在测试类的random属性的值是：#{T(org.apache.commons.lang3.RandomUtils).nextInt(1,100)}")
	private String propertyRandom;

	@Value("容器中的random值是：#{random}")
	private String beanRandom;

	@Test
	@Transactional
	@Sql(scripts = { "classpath:sql/lineTeam.sql",
			"classpath:sql/dept.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
	@Rollback
	public void test1() {
		List<BaseLineTeam> lineTeamList = lineTeamService.queryAll();
		lineTeamList.forEach(lineTeam -> System.out.println(lineTeam.getGroupName()));
	}

	@BeforeTransaction
	public void beforeTransaction() {
		System.out.println("TestParentCache.beforeTransaction()");
	}

	@AfterTransaction
	public void afterTransaction() {
		System.out.println("TestParentCache.afterTransaction()");
	}

	@Test
	@Repeat(3)
	@Ignore
	public void test2() {
		System.out.println(propertyRandom);
		System.out.println(beanRandom);
	}

	@Test
	@Repeat(3)
	@DirtiesContext(methodMode = MethodMode.BEFORE_METHOD, hierarchyMode = HierarchyMode.CURRENT_LEVEL)
	public void test3() {
		System.out.println(propertyRandom);
		System.out.println(beanRandom);
	}
}