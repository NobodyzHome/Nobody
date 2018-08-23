package com.spring.testframe;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.spring.data.dao.BaseDeptMapper;
import com.spring.data.dao.BaseLineTeamMapper;
import com.spring.data.dao.BasicMapper;
import com.spring.data.domain.BaseDept;
import com.spring.data.domain.BaseLineTeam;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/test/containers/test-basic.xml")
@ActiveProfiles("jiayuguan")
@TestPropertySource(properties = { "hello=你好", "world=世界" })
public class BasicTestCase {

	@Autowired
	private BasicMapper mapper;

	@Autowired
	private BaseDeptMapper deptMapper;

	@Autowired
	private BaseLineTeamMapper lineTeamMapper;

	@Value("2017-10-01 20:30:00")
	private Date startTime;

	@Value("2017-10-01 21:30:00")
	private Date endTime;

	@Value("${hello} and ${world}")
	private String helloWorld;

	@Autowired
	private ApplicationContext applicationContext;

	/*
	 * 关于测试类中的@PostConstruct和@PreDestroy方法：
	 * 
	 * 在每一次调用@Test方法前，都会调用测试类中的@PostConstruct方法，但容器不论什么情况，从不会调用测试类中的@
	 * PreDestroy方法。
	 * spring并不推荐使用@PostConstruct方法来控制每次执行@Test方法之前应执行的方法，而是应该使用Junit本身的@
	 * Before、@After等注解的方法。
	 * 
	 * If a method within a test class is annotated with @PostConstruct, that
	 * method will be executed before any before methods of the underlying test
	 * framework (e.g., methods annotated with JUnit 4’s @Before), and that will
	 * apply for every test method in the test class. On the other hand, if a
	 * method within a test class is annotated with @PreDestroy, that method
	 * will never be executed. Within a test class it is therefore recommended
	 * to use test lifecycle callbacks from the underlying test framework
	 * instead of @PostConstruct and @PreDestroy.
	 */
	@PostConstruct
	public void init() {
		System.out.println("BasicTest.init()");
	}

	@PreDestroy
	public void destroy() {
		System.out.println("BasicTest.destroy()");
	}

	@Test
	@Transactional
	public void test1() {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("startTime", startTime);
		paramMap.put("endTime", endTime);

		mapper.callFunction(paramMap);

		Integer interval = (Integer) paramMap.get("interval");
		System.out.println(interval);
	}

	@BeforeTransaction
	public void test2() {
		System.out.println("BasicTestCase.test2()");
	}

	@AfterTransaction
	public void test3() {
		System.out.println("BasicTestCase.test3()");
	}

	@Repeat(3)
	@Test
	public void test4() {
		System.out.println("BasicTestCase.test4()");
	}

	@Test
	@Timed(millis = 5000)
	public void test5() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("BasicTestCase.test5()");
	}

	@Test
	public void test6() {
		System.out.println(helloWorld);
	}

	@Test
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	public void test7() {
		List<String> list = applicationContext.getBean("list", List.class);
		list.remove(0);
		list.forEach(System.out::println);
		System.out.println("BasicTestCase.test7()");
	}

	@Test
	public void test8() {
		List<String> list = applicationContext.getBean("list", List.class);
		list.forEach(System.out::println);
		System.out.println("BasicTestCase.test8()");
	}

	@Test
	@Transactional
	@Sql("classpath:sql/dept.sql")
	@Sql("classpath:sql/lineTeam.sql")
	public void test9() {
		List<BaseDept> deptList = deptMapper.queryDepts("%测试%");
		deptList.forEach(dept -> System.out.println(dept.getDeptName()));

		List<BaseLineTeam> lineTeamList = lineTeamMapper.queryAll();
		lineTeamList.forEach(
				lineTeam -> System.out.println(lineTeam.getDescription() + ":" + lineTeam.getDept().getDeptName()));
	}

	@IfProfileValue(name = "user.name", value = "maziqiang")
	@Test
	public void test10() {
		System.out.println("BasicTestCase.test10()");
	}

	@Test
	public void test11() {
		/*
		 * 下面这句话有两个作用：
		 * 
		 * a) 给变量test赋值，赋的值是"sdf123"这个对象的内存地址
		 * 
		 * b)
		 * 返回赋值表达式的返回值，也就是"sdf123"。当然，这里没有对赋值表达式的返回值做任何操作，因此下面这句话实际产生的只是赋值的效果。
		 */
		String text = "sdf" + 123;
		System.out.println(text);

		// 输出text="efg"这个表达式的返回值，也就是输出efg
		System.out.println(text = "efg");
		System.out.println(text);

		// 把text = "zxc" + 123这个表达式的返回值赋值给newText这个变量
		String newText = (text = "zxc" + 123);
		System.out.println(newText);
	}
}
