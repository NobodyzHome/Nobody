package com.spring.testframe;

import java.util.List;

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
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.ErrorMode;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.web.WebTestContextBootstrapper;
import org.springframework.transaction.annotation.Transactional;

import com.spring.data.dao.BaseDeptMapper;
import com.spring.data.dao.BaseLineTeamMapper;
import com.spring.data.dao.BaseRoleMapper;
import com.spring.data.domain.BaseDept;
import com.spring.data.domain.BaseLineTeam;
import com.spring.data.domain.BaseRole;

/*
 * @RunWith是Junit的注解，只有设置该注解的值为SpringRunner，那么Spring测试框架的所有注解才起效果。
 */
@RunWith(SpringRunner.class)
/*
 * 注意：@TestPropertySource提供的键值对，不能用于容器的<bean>中的占位符的替换。因此，如果一个<bean>的属性值中有${...}
 * 样式的占位符，还是需要通过context:property-placeholder来替换。
 */
@TestPropertySource(properties = { "hello=你好", "world=世界" }, inheritProperties = true)
/*
 * @ContextHierarchy使用的注意：
 * 
 * 一、第一种使用方式：在一个类中设置@ContextHierarchy，配置多个@ContextConfiguration
 * 
 * 在@ContextHierarchy中配置的多个@ContextConfiguration，每一个@ContextConfiguration会生成一个容器
 * ，并且在前面的@ContextConfiguration对应的容器是在后面的@ContextConfiguration对应的容器的父容器。
 * 
 * 二、第二种使用方式：在子类中设置@ContextHierarchy，子类中的容器是父类的容器的子容器
 * 
 * @ContextHierarchy不仅可以用于在一个类中设置多个容器的父子关系，还可以在继承的类中设置容器的父子关系。例如如果类A设置了@
 * ContextConfiguration，而类B（继承自类A）设置了@ContextHierarchy，那么类A对应的容器是类B对应的容器的父容器。
 * 而如果类B只设置了@ContextConfiguration，那么实际上此时类B对应的容器就没有父容器了，而是一个拥有类A的@
 * ContextConfiguration配置和类B的@ContextConfiguration配置的容器。
 * 
 * 另一个需要注意的问题是：
 * 
 * 如果在一个类上设置了@ContextHierarchy(A,B,C)这三个@ContextConfiguration，
 * 那么可以看到A容器是B容器的父容器，B容器是C容器的父容器。但是在这个类中使用@Autowire获得ApplicationContext的对象时，
 * 实际上只会获得一个容器对象，获得的是容器C。
 */
@ContextHierarchy({ @ContextConfiguration("classpath:spring/test/containers/data-config.xml"),
		@ContextConfiguration("classpath:spring/test/containers/data-service.xml") })
/*
 * @ActiveProfiles用于指定容器所激活的profile。
 * 
 * 注意：这里的profile是用于激活容器中指定profile的<beans>下的配置，和@IfProfileValue中的profile的作用还是有区别的
 * 。@IfProfileValue中的profile用于判断@Test方法是否被执行。
 */
@ActiveProfiles(inheritProfiles = true, profiles = "test")
/*
 * 该注解用于配置@IfProfileValue所使用的数据源，如果没有配置该注解，则使用默认的SystemProfileValueSource类作为数据源，
 * 其获取key的value的方式是使用System.getProperty(key);来获取，也就是从系统属性中获取指定key对应的value。
 */
@ProfileValueSourceConfiguration
/*
 * 如果@IfProfileValue注解在类上，那么该类上的所有@Test方法，只有在
 * ProfileValueSource.get("user.name").equals("maziqiang")的情况下（
 * ProfileValueSource是通过@ProfileValueSourceConfiguration配置的），才会执行该类下的所有@Test方法，
 * 否则则忽略该类下的@Test方法，相当于在@Test方法上增加了@Ignore。
 * 
 * 注意：@IfProfileValue既可以注解到Class上，也可以注解到Method上。但比较特殊的是，类上的@
 * IfProfileValue配置会覆盖方法上的@IfProfileValue配置，也就是说如果类上的@
 * IfProfileValue不满足，那么整个类的@Test方法都被忽略，不论类里的@Test方法上的@IfProfileValue配置是否满足
 * 但是如果类上的@IfProfileValue满足了，但是某一个@Test方法上的@IfProfileValue不满足，那么这个@Test方法依然不会执行
 * 
 * @IfProfileValue和@ActiveProfiles的区别：
 * 
 * @IfProfileValue和@ActiveProfiles虽然都涉及到profile的概念，但使用profile的场景不同。@
 * IfProfileValue用于根据获取的key的value值来控制@Test方法是执行还是被忽略，而@
 * ActiveProfiles用于控制容器的配置文件中，哪些<beans>下的配置需要被注册到容器中。
 */
@IfProfileValue(name = "user.name", value = "maziqiang")
/*
 * @SqlConfig可以配置：
 * 
 * 1.执行的@Sql文件的注释样式commentPrefix，换行符样式separator，
 * 块状注释样式blockCommentStartDelimiter和blockCommentEndDelimiter
 * 
 * 2.@SqlConfig还可以配置执行@Sql脚本所使用的transactionManager，
 * 事务隔离状态transactionMode以及执行sql脚本发生错误时的处理方案errorMode
 * 
 * @SqlConfig可以有公共和私有两种配置。如果是公共的，则配置到类上，该类下的所有@Sql都会统一使用该配置。如果是私有的，则配置到@
 * Sql注解的config属性中，作为该@Sql私有的配置。
 * 
 * 关于transactionMode = TransactionMode.ISOLATED：
 * 
 * 默认情况下，spring是在@Test方法所开启的事务中执行@Sql脚本的，但我们也可以设置transactionMode为ISOLATED，
 * 那么会在一个新的事务里执行@Sql脚本，并且在@Sql脚本执行完后会立即提交这个新事务，不论@Test上是@Commit还是@Rollback。
 * 实际上，@Test上的@Commit或@Rollback控制的是@Test方法执行完成后，是进行事务提交还是回滚。
 */
@SqlConfig(commentPrefix = "--", separator = ";", encoding = "UTF-8", transactionManager = "transactionManager", errorMode = ErrorMode.CONTINUE_ON_ERROR, transactionMode = TransactionMode.ISOLATED)

/*
 * @TestExecutionListeners用于指定向TestContextManager中所注册的TestExecutionListener
 * 
 * listeners属性指定了要注册的TestExecutionListener接口的实现类
 * inheritListeners属性指定了在一个类上加上的@TestExecutionListeners注解，是否能被这个类的子类所继承
 * mergeMode属性指定了是否要继承父类上已注册的TestExecutionListener，如果值为REPLACE_DEFAULTS，
 * 则只注册当前类上listeners属性指定的TestExecutionListener，父类上的不会被继承过来。
 * 而如果值为MERGE_WITH_DEFAULTS，则会继承父类上的TestExecutionListener。
 * 
 * 注意：spring会剔除重复注册的TestExecutionListener实现类，
 * 例如默认情况下已注册了DependencyInjectionTestExecutionListener，
 * 那么我们的类再注册一个DependencyInjectionTestExecutionListener。
 * 在TestContextManager中也只会有一个DependencyInjectionTestExecutionListener类的实例，不会有两个。
 */
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class,
		DirtiesContextBeforeModesTestExecutionListener.class }, inheritListeners = true, mergeMode = MergeMode.REPLACE_DEFAULTS)
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
public class TestSummary {

	/*
	 * 在测试类中，可以使用@Autowired,@Resource,@Required,@Value等注解。并且，在每次执行@Test方法之前，
	 * 该测试类上使用的这些注解都会被重新执行一遍。
	 */
	@Autowired
	private BaseLineTeamMapper lineTeamMapper;

	@Autowired
	private BaseDeptMapper deptMapper;

	@Autowired
	private BaseRoleMapper roleMapper;

	@Value("${hello} and ${world} and #{T(org.apache.commons.lang3.RandomUtils).nextInt(1,100)}")
	private String helloWorld;

	@Test
	/*
	 * 可以将@Transactional注解到@Test方法上，那么spring在调用@Test方法前，
	 * 使用@Transactional指定的TransactionManager开启事务，在调用@Test方法后，
	 * 使用TransactionManager提交或回滚事务（具体是提交还是回滚事务，取决于@Test方法上是@Rollback注解还是@Commit）
	 * 在默认情况下，spring在执行完@Test方法后，都会使用TransactionManager进行事务的回滚操作，因此相当于在@
	 * Test方法上增加了@Rollback注解。如果希望在执行完@Test方法后，使用TransactionManager进行事物的提交操作，
	 * 则需要在@Test方法上增加@Commit注解。
	 */
	@Transactional
	/*
	 * @Sql可以注解在一个类或方法上，用于指定在执行@Test方法前，需要执行的脚本。
	 * 
	 * @SqlGroup也可以注解在一个类或方法上，用于指定在执行@Test方法前，执行多个@Sql脚本。
	 * 
	 * 由于java8的新特性，可以在@Test方法上写多个@Sql注解。但实际上这也是一个语法糖，其实际也是把这多个@Sql注解创建到@
	 * SqlGroup注解的value属性中。具体可以看到，在@Sql注解的代码上有@Repeatable(SqlGroup.class)，
	 * 说明在同一个地方设置的多个@Sql注解，其实际会创建一个SqlGroup注解类的对象，并将多个@
	 * Sql注解对象放到SqlGroup对象的value属性中。
	 * 
	 * 注意：spring默认是在当前@Test方法对应的TransactionManager所开启的事务中执行@Sql脚本，因此@Test方法上是@
	 * Rollback还是@Commit，决定了@Sql脚本的数据操作是否会保存到数据库中。
	 * 
	 * 如果需要@Sql脚本在指定的TransactionManager所开启的事务中执行，那么可以使用@SqlConfig，来配置执行@
	 * Sql脚本所使用的TransactionManager
	 */
	@Sql("classpath:sql/dept.sql")
	// 可以通过指定executionPhase属性来指定要执行的sql语句是在@Test方法执行之前执行，还是在@Test方法执行之后执行
	@Sql(scripts = "classpath:sql/lineTeam.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
	/*
	 * 从spring4.2开始，@Sql注解中增加了statemnts属性，可以直接在该属性中给出要执行的sql语句，
	 * 而不必将sql语句写到一个sql文件中，然后执行这个sql文件。
	 */
	@Sql(statements = { "insert into base_dept values(6666,'6666','测试公司4',6,11,systimestamp,1)",
			"insert into base_dept values(5555,'5555','测试公司5',6,11,systimestamp,1)" })
	@Sql(statements = "insert into ext_base_line_team values(7777,'路队7777','19,20,21,22,23',9999)")
	// 由于在默认情况下，spring在执行完@Test方法后，会执行事务回滚操作，因此该@Rollback注解可以不用给出。
	@Rollback
	public void test1() {
		List<BaseLineTeam> lineTeamList = lineTeamMapper.queryAll();
		List<BaseDept> deptList = deptMapper.queryDepts("%公司%");

		lineTeamList.forEach(lineTeam -> System.out.println(lineTeam.getGroupName() + ":" + lineTeam.getDescription()));
		deptList.forEach(dept -> System.out.println(dept.getDeptName()));
	}

	@Test
	/*
	 * 关于@Repeat的注意：
	 * 
	 * @Repeat是重复调用后面的Statement指定次数。一般来说，
	 * Repeat后面的Statement有RunAfterTestMethodCallbacks,RunAfters,
	 * RunBeforeTestMethodCallbacks,RunBefores,InvokeMethod。
	 * 也就是说这五个Statement会被执行多次。
	 */
	@Repeat(3)
	public void test2() {
		System.out.println(helloWorld);
	}

	@Test
	public void test3() {
		System.out.println(helloWorld);
	}

	/*
	 * @BeforeTransaction和@AfterTransaction注解的不是一个@Test方法，而是注解在一个普通的方法上，当有注解了@
	 * Transactional的@Test方法被执行之前或之后，即TransactionManager开启事务之前和结束事务之后，会分别调用@
	 * BeforeTransaction和@AfterTransaction的方法
	 */
	@BeforeTransaction
	public void beforeTransaction() {
		System.out.println("TestExcercise.beforeTransaction()");
	}

	@AfterTransaction
	public void afterTransaction() {
		System.out.println("TestExcercise.afterTransaction()");
	}

	@Test
	@Transactional
	/*
	 * @DirtiesContext可以把当前测试方法对应的容器标记为“脏的”，一个容器对象被设置成“脏的”后，spring立即从缓存中清除该容器对象
	 * ，并将该容器对象关闭。那么在随后的@Test方法被调用前，spring会重新加载这个容器的配置文件，生成一个容器，并将这个容器存储到缓存中。
	 * 
	 * When an application context is marked dirty, it is removed from the
	 * testing framework’s cache and closed. As a consequence, the underlying
	 * Spring container will be rebuilt for any subsequent test that requires a
	 * context with the same configuration metadata.
	 * 
	 * @DirtiesContext可以设置在@Test方法中：
	 * 
	 * 1.methodMode = BEFORE_METHOD，在当前@Test方法被执行前，将容器设置为“脏的”
	 * 
	 * 2.methodMode = AFTER_METHOD，在当前@Test方法被执行后，将容器设置为“脏的”
	 * 
	 * @DirtiesContext不仅可以设置在方法中，也可以设置在类中：
	 * 
	 * 1.classMode = BEFORE_CLASS，在当前测试类被执行前，将容器设置为“脏的”
	 * 
	 * 2.classMode = AFTER_CLASS，在当前测试类被执行后，将容器设置为“脏的”
	 * 
	 * 3.classMode = BEFORE_EACH_TEST_METHOD，当前测试类的每一个@Test方法被执行前，将容器设置为“脏的”
	 * 
	 * 4.classMode = AFTER_EACH_TEST_METHOD，当前测试类的每一个@Test方法被执行后，将容器设置为“脏的”
	 * 
	 * 注意：
	 * hierarchyMode=CURRENT_LEVEL，那么只把容器等级结构中的当前容器设置为“脏的”，该容器的父容器不会被设置为“脏的”。
	 * 而如果hierarchyMode=EXHAUSTIVE，那么不仅会把当前容器设置为“脏的”，还会把该容器的所有祖先容器（ancestor）
	 * 也都设置为“脏的”。 默认使用的是EXHAUSTIVE模式。
	 * 
	 * By default an exhaustive algorithm will be used that clears the context
	 * cache including not only the current level but also all other context
	 * hierarchies that share an ancestor context common to the current test;
	 */
	@DirtiesContext(methodMode = MethodMode.BEFORE_METHOD, hierarchyMode = HierarchyMode.CURRENT_LEVEL)
	@Sql("classpath:sql/role.sql")
	// 注意：@Commit和@Rollback注解都可以注解到类上，对类下的所有@Transactional的@Test方法都产生效果。
	@Commit
	public void test4() {
		int[] roleNoArray = { 9999, 8888, 7777, 6666 };
		List<BaseRole> roleList = roleMapper.queryRoles(roleNoArray, null);
		roleList.forEach(role -> System.out.println(role.getRoleName()));
	}

	@Test
	/*
	 * @Timed注解用于控制@Test方法所执行的时间，如果@Test方法所执行的时间超过@Timed注解所设置的时间限制，则该@
	 * Test方法则会被认为是执行失败。在该@Test方法中则会报出错误java.util.concurrent.TimeoutException:
	 * Test took 2016 ms; limit was 1000 ms.
	 */
	@Timed(millis = 1000)
	@Transactional
	public void test5() {
		List<BaseDept> deptList = deptMapper.queryDepts("%部%");
		deptList.forEach(dept -> System.out.println(dept.getDeptName()));
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	/*
	 * 在@IfProfileValue的values属性中，可以给出多个值，
	 * 只要使用key从ProfileValueSource中获得的值能匹配这多个值中的其中一个，就会执行该@Test方法
	 */
	@IfProfileValue(name = "user.name", values = { "lisi", "zhangsan", "wangwu", "maziqiang" })
	public void test6() {
		System.out.println("TestExcercise.test6()");
	}
}