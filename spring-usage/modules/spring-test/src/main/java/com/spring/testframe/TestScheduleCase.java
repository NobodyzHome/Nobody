package com.spring.testframe;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.ErrorMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

@RunWith(SpringRunner.class)
@ContextHierarchy({ @ContextConfiguration("classpath:spring/test/containers/data.xml"),
		@ContextConfiguration("classpath:spring/test/containers/busness.xml") })
@TestPropertySource(properties = { "fixedDelay=5000", "clearCacheOffset=1000" })
@ActiveProfiles("test")
@SqlConfig(errorMode = ErrorMode.CONTINUE_ON_ERROR)
@ProfileValueSourceConfiguration
public class TestScheduleCase {

	@Test
	@Sql("classpath:sql/lineTeam.sql")
	@IfProfileValue(name = "user.name", value = "maziqiang")
	@Timed(millis = 3000)
	@Repeat(3)
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD, hierarchyMode = HierarchyMode.CURRENT_LEVEL)
	public void test1() {
		System.out.println("TestScheduleCase.test1()");
	}

	@BeforeTransaction
	public void beforeTransaction() {
		System.out.println("TestScheduleCase.beforeTransaction()");
	}

	@AfterTransaction
	public void afterTransaction() {
		System.out.println("TestScheduleCase.afterTransaction()");
	}

	@Before
	public void before() {
		System.out.println("TestScheduleCase.before()");
	}

	@After
	public void after() {
		System.out.println("TestScheduleCase.after()");
	}

	@BeforeClass
	public static void beforeClass() {
		System.out.println("TestScheduleCase.beforeClass()");
	}

	@AfterClass
	public static void afterClass() {
		System.out.println("TestScheduleCase.afterClass()");
	}

}
