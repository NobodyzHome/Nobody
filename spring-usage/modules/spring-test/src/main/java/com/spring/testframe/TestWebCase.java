package com.spring.testframe;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.servlet.ServletContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.context.web.WebTestContextBootstrapper;
import org.springframework.util.StreamUtils;

@RunWith(SpringRunner.class)
@ContextConfiguration
/*
 * 当设置@WebAppConfiguration后，则创建的是一个WebApplicationContext容器，即一个Web环境的容器。我们可以配置@
 * WebAppConfiguration的value属性，来配置web项目的根目录。如果没有配置该属性，默认的web项目的根目录是file:src/main
 * /webapp。
 * 
 * 注意：@WebAppConfiguration必须和@ContextConfiguration联合使用。
 */
@WebAppConfiguration("file:src/test/webapp")
/*
 * @BootstrapWith用于指定TestContextBootstrapper的实现类。
 * 
 * TestContextBootstrapper用于启动SpringTest框架，它提供了以下方法：
 * 1.buildTestContext()。创建当前测试类对应的TestContext对象
 * 2.buildMergedContextConfiguration()。根据当前测试类上的容器配置（例如@ContextConfiguration、@
 * ActiveProfile、@TestPropertySource），创建MergedContextConfiguration对象
 * 3.getTestExecutionListeners()。获取已注册的TestExecutionListener对象的集合。
 * 
 * spring提供的默认的TestContextBootstrapper的实现类可以满足绝大部分测试的需求，因此一般情况下无需进行该注解的配置
 */
@BootstrapWith(WebTestContextBootstrapper.class)
public class TestWebCase {

	@Configuration
	public static class ContextConfig {

		private InputStream deptPage;

		// 由于当前容器是Web容器，因此可以自动装配ServletContext对象。实际上装配的是MockServletContext类的实例
		@Autowired
		public void deptPage(ServletContext servletContext) {
			deptPage = servletContext.getResourceAsStream("WEB-INF/pages/dept.jsp");
		}
	}

	@Autowired
	private ContextConfig config;

	@Test
	public void test1() throws IOException {
		String contents = StreamUtils.copyToString(config.deptPage, Charset.defaultCharset());
		System.out.println(contents);
	}
}
