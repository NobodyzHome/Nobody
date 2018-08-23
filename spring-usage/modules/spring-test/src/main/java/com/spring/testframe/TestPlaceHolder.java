package com.spring.testframe;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.ServletConfigPropertySource;
import org.springframework.web.context.support.ServletContextPropertySource;
import org.springframework.web.context.support.StandardServletEnvironment;

@RunWith(SpringRunner.class)
@ContextConfiguration
@WebAppConfiguration
@TestPropertySource(properties = { "hello=你好", "world=${hello} 世界!" })
public class TestPlaceHolder {

	@Configuration
	// 注意：@PropertySource用于创建一个PropertySource对象，并将该对象注册到Environment所使用的PropertySources中
	@PropertySource(name = "userSource", value = { "classpath:database/datasource.cfg.properties",
			"file:src/main/resources/database/jiayuguan.properties" })
	static class ContextConfiguration {
		@Bean
		@Scope(scopeName = WebApplicationContext.SCOPE_SESSION)
		public String hello(@Value("${hello}") String hello) {
			return hello;
		}

		@Bean
		@Scope(WebApplicationContext.SCOPE_REQUEST)
		public String world(@Value("#{'${hello}'+' and '+'${world}'}") String world) {
			return world;
		}

	}

	private Environment environment;

	@SuppressWarnings("unused")
	@Autowired
	private String hello, world;

	// spel內联Map的格式是{key1:value1,key2:value2}，其中key和value都可以不是String类型的
	@Value("#{{'zhangsan':'张三','lisi':'李四','time':new java.util.Date()}}")
	private Map<String, Object> map;

	private ServletContext servletContext;

	private ServletConfig servletConfig;

	@Autowired
	private void initServletContext(ServletContext servletContext) {
		servletContext.setInitParameter("yuguofei", "于国飞");
		this.servletContext = servletContext;
	}

	// 注意：即使一个方法中没有参数，也可以使用@Autowired注解，来让容器在对<bean>的实例的自动装配阶段来调用该方法
	@Autowired
	public void initServletConfig() {
		servletConfig = new MockServletConfig();
		MockServletConfig mockServletConfig = (MockServletConfig) servletConfig;
		mockServletConfig.addInitParameter("wangtianlong", "王天龙");
	}

	@Autowired
	private void initEnvironment(Environment environment) {
		StandardServletEnvironment servletEnvironment = (StandardServletEnvironment) environment;
		servletEnvironment.initPropertySources(servletContext, servletConfig);

		this.environment = environment;
	}

	@Before
	public void showSplitor() {
		System.out.println(StringUtils.center("我是分隔符", 50, "="));
	}

	// 测试在容器环境下进行字符串的占位符的替换
	@Test
	public void testContextEnvironment() {
		/*
		 * 在ApplicationContext的环境中，直接通过自动装配并使用Environment对象即可，
		 * 因为Environment继承自PropertyResolver，具有对字符串中的占位符进行替换的功能。
		 * 
		 * 使用Environment，能获取以下资源，用于替换字符串中的占位符：
		 * 
		 * 1.系统属性和环境变量（可以通过mvn help:system来查询系统属性和环境变量）
		 * 
		 * 2.@PropertySource中提供的资源
		 * 
		 * 3.session attribute（需要web环境）
		 * 
		 * 4.request attribute（需要web环境）
		 * 
		 * 5.servletContext initParam（需要web环境）
		 * 
		 * 6.servletConfig initParam中获取键值对（需要web环境）
		 */
		System.out.println(environment.resolvePlaceholders("system property [user.name] is : ${user.name}"));
		System.out.println(environment.resolvePlaceholders("system environment property [HOME] is : ${HOME}"));
		System.out.println(environment.resolvePlaceholders(
				"@PropertySource property [datasource.default.maxTotal] is : ${datasource.default.maxTotal}"));
		System.out.println(environment.resolvePlaceholders("session attribute [hello] is : ${hello}"));
		System.out.println(environment.resolvePlaceholders("request attribute [world] is : ${world}"));
		System.out.println(environment.resolvePlaceholders("servlet context init param [yuguofei] is : ${yuguofei}"));
		System.out.println(
				environment.resolvePlaceholders("servlet config init param [wangtianlong] is : ${wangtianlong}"));
	}

	// 测试在非容器环境下进行字符串中的占位符的替换
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testNonContextEnvironment() throws IOException {
		// 在非容器环境下实现字符串中的占位符替换的思路创建PropertySroucesPropertyResolver，由其来进行字符串中的占位符的替换

		// 一、创建PropertySource。每一个PropertySource用来存储多个键值对资源
		// a) 创建一个MapPropertySource，从Map中获取键值对资源
		MapPropertySource mapSource = new MapPropertySource("mapSource", map);
		// 增加系统环境的PropertySource
		MapPropertySource systemEnvMapSource = new MapPropertySource("mapSource", (Map) System.getenv());

		// b) 创建PropertiesPropertySource，从Properties中获取键值对资源
		Properties properties = new Properties();
		properties.put("luna", "露娜");
		properties.put("cookie", "小饼干");
		PropertiesPropertySource propertiesSource = new PropertiesPropertySource("propertiesSource", properties);
		// 增加系统属性的PropertySource
		PropertiesPropertySource systemPropertiesSource = new PropertiesPropertySource("systemPropertiesSource",
				System.getProperties());

		// c) 创建ResourcePropertySource，从指定的Resource对象中获取键值对资源
		ResourcePropertySource resourceSource = new ResourcePropertySource(
				"file:src/main/resources/database/test196.properties");

		// d) 创建ServletContextPropertySource，从ServletContext的initParam中获取键值对资源
		// （注意：不是从ServletContext的getAttribute）中获取键值对资源
		ServletContextPropertySource servletContextSource = new ServletContextPropertySource("servletContext",
				servletContext);

		// e) 创建ServletConfigPropertySource，从ServletConfig的initParam中获取键值对资源
		ServletConfigPropertySource servletConfigSource = new ServletConfigPropertySource("servletConfig",
				servletConfig);

		// 二、创建PropertySources，来存储已经创建的多个PropertySource对象
		MutablePropertySources propertySources = new MutablePropertySources();
		propertySources.addLast(mapSource);
		propertySources.addLast(systemEnvMapSource);
		propertySources.addLast(propertiesSource);
		propertySources.addLast(systemPropertiesSource);
		propertySources.addLast(resourceSource);
		propertySources.addLast(servletContextSource);
		propertySources.addLast(servletConfigSource);

		// 三、创建PropertyResolver。PropertyResolver的resolveRequiredPlaceholders方法可以对一个字符串中的占位符进行替换
		PropertyResolver propertyResolver = new PropertySourcesPropertyResolver(propertySources);

		System.out.println(propertyResolver.resolvePlaceholders("Map property [lisi] is : ${lisi}"));
		System.out.println(propertyResolver.resolvePlaceholders("Properties property [luna] is : ${luna}"));
		System.out.println(propertyResolver.resolvePlaceholders("System property [user.name] is : ${user.name}"));
		System.out.println(propertyResolver.resolvePlaceholders("System environment property [SHELL] is : ${SHELL}"));
		System.out.println(
				propertyResolver.resolvePlaceholders("Resource property [db.test.196.url] is : ${db.test.196.url}"));
		System.out.println(
				propertyResolver.resolvePlaceholders("ServletContext initParam property [yuguofei] is : ${yuguofei}"));
		System.out.println(propertyResolver
				.resolvePlaceholders("ServletConfig initParam property [wangtianlong] is : ${wangtianlong}"));
	}
}
