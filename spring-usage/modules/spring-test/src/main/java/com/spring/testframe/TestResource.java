package com.spring.testframe;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.StreamUtils;

@RunWith(SpringRunner.class)
/*
 * 如果只给出了@ContextConfiguration，没有给出任何容器的配置，那么spring则默认从当前类中的带有@
 * Configuration注解的静态内部类中获取容器的配置
 */
@ContextConfiguration
@WebAppConfiguration("file:src/test/webapp")
public class TestResource {

	/*
	 * @ContextConfiguration对静态类的要求：
	 * 
	 * 1.静态类的访问修饰符不能是private，其余均可以
	 * 
	 * 2.静态类必须是被@Configuration注解，而不能是@Component
	 */
	@Configuration
	static class ContextConfig {

		// 对当前组件类中的属性进行自动装配，因为组件类也会被注册到容器中，成为容器中的<bean>，因此组件类中自然可以享受自动装配等福利
		@Autowired
		private ResourceLoader resourceLoader;

		/*
		 * 如果路径前缀是file，并且路径不是以"/"开头，那么获取的是基于当前项目下的路径。
		 * 
		 * 以本例来说，file:src/java/resources给出的是不以"/"开头的路径src/java/resources，
		 * 假设当前项目的路径为/Users/maziqiang/Workspaces
		 * /spring-test，那么该Resource对象则获取的是以下目录/Users/maziqiang/Workspaces/spring
		 * -test/src/java/resources
		 */
		@Value("file:src/java/resources")
		private Resource fileResourceRelative;

		/*
		 * 获取FileSystemResource。由于路径以"/"开头，因此获取的是基于操作系统的根目录下的路径。
		 * 即获取"/maziqiang/test"的路径。
		 */
		@Value("file:/maziqiang/test")
		private Resource fileResourceAbsolute;

		/*
		 * 获取ClassPathResource。假设当前项目的classpath为zndd/target/classes，
		 * 那么下例则获取的是zndd/target/classes/database/datasource.cfg.
		 * properties路径下的资源。
		 */
		@Value("classpath:database/datasource.cfg.properties")
		private Resource classpathResource;

		/*
		 * 获取UrlResource。
		 */
		@Value("http://www.baidu.com")
		private Resource urlResource;

		/*
		 * 获取ServletContextResource。在web容器的环境下，如果没有给出路径前缀，获取的基于web项目下的路径。
		 * 以本例来说，假设当前web项目的路径是/tomcat9/webapps/zndd，那么这个Reource获取的则是/tomcat9/
		 * webapps/zndd/WEB-INF/pages/dept.jsp路径下的内容
		 */
		@Value("WEB-INF/pages/dept.jsp")
		private Resource webResource;

		/*
		 * 关于claspath:和classpath*:的区别的总结
		 * 
		 * classpath:仅从classpath中当前项目的输出目录中获取资源，不会尝试从classpath下的jar包中获取对应路径的资源
		 * classpath*:不仅从classpath中当前项目的输出目录中获取资源，还会尝试从classpath下的jar包中获取对应路径的资源
		 * 
		 * 因此，classpath:与classpath*:的唯一区别就在于classpath:仅从当前项目的输出目录获取资源，
		 * 而classpath*:既从当前项目的输出目录获取资源，还从当前项目所引用的jar包中获取资源。
		 */
		// 仅从当前项目的输出目录中获取database/datasource.cfg.properties路径指定的资源
		@Value("classpath:database/datasource.cfg.properties")
		private Resource[] resources1;

		// 不仅从当前项目的输出目录中获取database/datasource.cfg.properties路径指定的资源，还会从当前项目引用的jar包中获取指定路径的资源
		@Value("classpath*:database/datasource.cfg.properties")
		private Resource[] resources2;

		// 仅从当前项目的输出目录中寻找指定路径对应的资源
		@Value("classpath:database/**/*.properties")
		private Resource[] resources3;

		// 不仅从当前项目的输出目录中寻找指定路径对应的资源，还会从当前项目引用的jar中获取指定路径的资源
		@Value("classpath*:database/**/*.properties")
		private Resource[] resources4;

		// 不仅是classpath前缀可以使用ant路径，file前缀也可以使用ant路径。但前缀也只能是file:，不能是file*:
		@Value("file:/Users/maziqiang/Downloads/*")
		private Resource[] resources5;

		@PostConstruct
		private void init() {
			System.out.println("TestResource.ContextConfig.init()");
		}

		@PreDestroy
		private void destroy() {
			System.out.println("TestResource.ContextConfig.destroy()");
		}

		// 定义向容器中注册的<bean>
		@Bean
		public Resource resource2() {
			/*
			 * 如果路径前缀是file，并且路径以"/"开头，那么获取的是当前系统的根目录下的路径。
			 * 
			 * 以本例来说，file:/maziqiang/test给出的是以"/"开头的路径/maziqiang/test，
			 * 那么该Resource对象获取的就是系统根目录下的/maziqiang/test的路径。
			 * 
			 * 注意： ResourceLoader在加载classpath:前缀的路径时，
			 * 是使用ClassLoader的getResource方法来获取指定的资源。而在加载file:前缀的路径时，
			 * 使用的是创建File对象来获取指定资源。
			 * 同时，ClassPathResource的getURL等方法不允许给出的路径在操作系统中不存在，
			 * 而FileSystemResource的getURL等方法允许给出的路径在操作系统中不存在。
			 */
			Resource resource = resourceLoader.getResource("file:/maziqiang/test");
			return resource;
		}

		public void showResource() {
			Resource[] resources = { fileResourceRelative, fileResourceAbsolute, classpathResource, webResource,
					urlResource };

			Arrays.stream(resources).forEach(resource -> {
				try {
					System.out.println(resource.getClass());
					if (resource.exists()) {
						InputStream inputStream = resource.getInputStream();
						String contents = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
						System.out.println(contents);
					} else {
						System.out.println("【不存在】：" + resource);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					System.out.println("========================");
				}
			});
		}
	}

	@Autowired
	private ContextConfig config;

	@Autowired
	private Resource resource2;

	@Autowired
	private ResourcePatternResolver resourcePatternResolver;

	@Test
	public void test1() throws IOException {
		System.out.println(config.fileResourceRelative.getFile().getAbsolutePath());
		System.out.println(config.fileResourceRelative.exists());

		System.out.println(resource2.getFile().getAbsolutePath());
		System.out.println(resource2.exists());

		System.out.println(StringUtils.center("分割线", 50, '='));

		config.showResource();

		Resource[] resources = resourcePatternResolver.getResources("classpath*:database/datasource.cfg.properties");
		System.out.println(resources);
	}
}
