package com.spring.testframe;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextHierarchy({ @ContextConfiguration("classpath:spring/test/containers/hierarchy3.xml"),
		@ContextConfiguration("classpath:spring/test/containers/hierarchy4.xml") })
public class TestHierarchy3 extends TestHierarchy2 {

	// 通过@Autowire获得的容器对象，是@ContextHierarchy等级结构中最下层的子容器对象
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void test1() {
		Map<String, String> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, String.class);
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
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	public void test4() {
		System.out.println("TestHierarchy3.test4()");
	}

	@Test
	public void test5() {
		test1();
	}
}