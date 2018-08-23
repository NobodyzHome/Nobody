package com.spring.testframe;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
// 当前类创建的容器以TestHierarchy1类创建的容器为父容器
@ContextHierarchy(@ContextConfiguration("classpath:spring/test/containers/hierarchy2.xml"))
public class TestHierarchy2 extends TestHierarchy1 {

	/*
	 * 注意：spring针对测试类中的@PostConstruct、@Autowired、@Resource等注解，在每次调用@Test方法前，
	 * 都会重新执行一次。例如在测试类中@Autowired注解了ApplicationContext类型的属性，那么在每次调用@Test方法前，
	 * 都会自动装配一下该类型的属性。
	 */
	@Autowired
	private ApplicationContext applicationContext;

	private Map<String, String> beans;

	// 该方法在每次调用@Test方法前，都会被执行
	@Autowired
	private void init(Map<String, String> beans) {
		System.out.println("TestHierarchy2.init()");
		this.beans = beans;
	}

	@Test
	public void test1() {
		beans.forEach((beanName, bean) -> System.out.println(beanName + ":" + bean));
	}

	@Test
	public void test2() {
		ApplicationContext parent = applicationContext.getParent();
		System.out.println((parent == null ? "无" : "有") + "父容器");
	}

	@Test
	public void test3() {
		ApplicationContext temp = applicationContext;
		int count = 0;

		while (temp.getParent() != null) {
			count += 1;
			temp = temp.getParent();
		}

		System.out.println("等级结构为" + (count + 1) + "层");
	}

	@Test
	@DirtiesContext(methodMode = MethodMode.BEFORE_METHOD, hierarchyMode = HierarchyMode.CURRENT_LEVEL)
	public void test4() {
		System.out.println("TestHierarchy2.dirtyContext()");
	}

	@Test
	public void test5() {
		test1();
	}
}