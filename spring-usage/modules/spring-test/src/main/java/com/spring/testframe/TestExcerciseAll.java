package com.spring.testframe;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.ErrorMode;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.spring.data.dao.BaseDeptMapper;
import com.spring.data.dao.BaseLineMapper;
import com.spring.data.dao.BaseLineTeamMapper;
import com.spring.data.domain.BaseDept;
import com.spring.data.domain.BaseLine;
import com.spring.data.domain.BaseLineTeam;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/test/containers/data-excercise.xml")
@SqlConfig(encoding = "UTF-8", transactionMode = TransactionMode.INFERRED, errorMode = ErrorMode.CONTINUE_ON_ERROR)
@TestPropertySource(properties = { "hello=你好", "world=世界" })
@ActiveProfiles("embedded")
public class TestExcerciseAll {

	@Autowired
	private BaseDeptMapper deptMapper;

	@Autowired
	private BaseLineTeamMapper lineTeamMapper;

	@Autowired
	private BaseLineMapper lineMapper;

	@Value("${hello}${world}#{T(org.apache.commons.lang3.RandomUtils).nextInt(1,100)}")
	private String random;

	@Value("2017-09-10 13:50:00")
	private Date startTime;

	@Value("2017-09-10 14:10:00")
	private Date endTime;

	// 每次执行@Test方法，都会创建一个新的测试类的实例。因此在调用test1、test2、test3这三个@Test方法时，都会分别创建一个当前测试类的实例。
	@Test
	@Transactional
	@Sql(statements = "insert into ext_base_line_team values(6666,'测试路队6','18路,28路,99路,101路',8888)")
	@Sql(statements = "insert into base_dept values(5555,'5555','测试公司5',7,11,CURRENT_TIMESTAMP,1)", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
	@Commit
	@IfProfileValue(name = "user.name", value = "maziqiang")
	public void test1() {
		List<BaseDept> deptList = deptMapper.queryDepts("%公司%");
		deptList.forEach(dept -> System.out.println(dept.getDeptName()));

		List<BaseLineTeam> lineTeamList = lineTeamMapper.queryAll();
		lineTeamList.forEach(
				lineTeam -> System.out.println(lineTeam.getGroupName() + ":" + lineTeam.getDept().getDeptName()));
	}

	@Test
	@Repeat(3)
	public void test2() {
		System.out.println(random);
	}

	@Test
	@Transactional
	@Sql(statements = "insert into base_dept values(123,'123','测试公司123',5,12,CURRENT_TIMESTAMP,0)", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
	public void test3() {
		BaseDept dept = deptMapper.queryDept(123);
		System.out.println(dept);
	}

	@Test
	@Transactional
	@Sql("classpath:sql/embedded/data/line.sql")
	@Commit
	public void test4() {
		BaseLine[] lineList = lineMapper.queryLineBasic(null, "路");
		Arrays.stream(lineList).forEach(line -> System.out.println(line.getLineName()));

		BaseLine[] lineList1 = lineMapper.queryLinesWithPrefix(null, null, true);
		Arrays.stream(lineList1)
				.forEach(line -> System.out.println(line.getLineName() + ":" + line.getDept().getDeptName()));
	}

	@Test
	@Transactional
	public void test5() {
		List<BaseDept> deptList = deptMapper.queryDeptAndLine(null);
		deptList.forEach(dept -> System.out.println(dept.getDeptName() + ":" + dept.getLines().size()));
	}

	@Test
	@Transactional
	public void test6() {
		BaseLine line = lineMapper.queryLineSelectDept(1111);
		System.out.println(line.getLineName() + ":" + line.getDept().getLines().size());

		BaseDept dept = deptMapper.queryDeptSelectLines(9999);
		System.out.println(dept);
	}

	@Test
	@Transactional
	@Ignore
	public void test7() {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("startTime", startTime);
		paramMap.put("endTime", endTime);

		lineMapper.callFunction(paramMap);
		System.out.println(paramMap.get("timeInterval"));
	}

	@Test
	@Sql("classpath:sql/embedded/data/line.sql")
	/*
	 * 这里如果加了@Transactional注解，那么会在执行该@Test方法时，创建数据库连接和事务，并将创建的数据库连接和当前线程绑定。
	 * 这就导致在该@Test方法中调用的Mapper的方法，实际使用的是同一个Session对象。因此，当多次调用Mapper方法时，
	 * 有可能会从Mybatis一级缓存（即Session对象）中获取数据。
	 * 如果想仅测试Spring的缓存，为了不让Mapper方法从同一个Session对象中获取缓存数据，需要把@Transactional注解去掉，
	 * 每次调用Mapper方法，都由Mapper自己创建数据库连接，因此使用的是不同的Session对象。
	 * 这样多次调用Mapper的方法使用的是不同的Session对象，就不会有多次调用Mapper的方法，使用同一个Session对象获取数据的问题了。
	 */
	// @Transactional
	public void test8() {
		/*
		 * 关于mybatis缓存在使用中遇到的一个问题：
		 * 之前在测试spring的Cache缓存，调用到Mapper的一个方法时，发现尽管传入的参数值不满足这个方法上的@
		 * Cacheable的condition的配置，但使用该参数值调用多次Mapper的这个方法，依然获取到是相同的对象。
		 * 遇到这个问题的第一反应是，这有可能是从mybatis的二级缓存中获取到的缓存结果。于是我就把mybatis的二级缓存给关闭了，
		 * 但执行起来依旧，使用该参数值调用这个Mapper的方法，获得的是相同的对象。这就很奇怪了，现在获得的既不是spring缓存里的数据，
		 * 也不是mybatis的二级缓存里的数据，那是哪儿的数据呢？
		 * 
		 * 后来一想，还有一个地方可以缓存数据，那就是mybatis的一级缓存，也就是Session中的缓存。
		 * 但这要求的是多次调用Mapper中的该方法，是使用同一个Session对象来进行查询的，这样才有可能从一级缓存中获取数据。
		 * 
		 * 那么现在多次调用Mapper的该方法，是不是使用的是同一个Session对象呢？于是我就跟代码，
		 * 该Mapper的方法会调用DefaultSqlSession的selectOne方法，我就在那个方法里打断点，
		 * 看多次调用Mapper的该方法时this变量的值是不是同一个对象。我发现竟然是同一个对象。
		 * 
		 * 后面我又查看Mybatis的日志，发现第一次调用Mapper的该方法，日志输出的是Creating a new
		 * SqlSession，而第二次调用该方法，日志输出的是Fetched SqlSession from current
		 * transaction。说明在调用Mapper的方法时，对于同一个事务，获得的确实是同一个Session对象。
		 * 
		 * 这就明白了，我在@Test方法中加了@Transactional注解，spring在执行该测试方法时，就已经开启了数据库连接和事务，在@
		 * Test方法中的Mapper就直接使用该数据库连接，由于上面说了，在调用Mapper的方法时，
		 * 对于相同的事务获取的是相同的Session对象，因此，在该@Test方法里，调用多次Mapper方法，
		 * 其实际使用的是同一个Session对象，因此当Spring的Cache没有满足condition条件，不从spring缓存中获取数据后，
		 * 就调用Mapper的方法，由于Mapper的该方法都是使用同一个Session对象，因此对于相同的参数值，
		 * 每次都是从这个Session对象中获取缓存数据。因此，Mapper的该方法实际上没有进行查询，
		 * 而是从这个Session对象的缓存中获取数据并返回。因此每次调用Mapper的该方法获得的是同一个对象。
		 * 
		 * 我要是仅想测Spring缓存，不想让Mybatis从一级缓存中取数据怎么办？
		 * 很简单，我们取消掉@Test方法上的@Transactional注解，这样spring就不会在执行@Test方法时开启数据库连接了，
		 * 那么每次调用Mapper的方法，都是由Mybatis自己来创建数据库连接，即每次调用Mapper方法使用的都是不同的数据库连接，
		 * 这就导致每次调用Mapper方法都使用不同的Sesion对象，
		 * 因此就没有多次调用Mapper方法时从同一个Session对象中取缓存数据的问题了，同时我们又关闭了二级缓存，
		 * 相当于调用Mapper的方法就肯定不会从Mybatis的缓存中获取数据了，这样我们就直接测试的就是spring的cache了。
		 * 
		 * 总结：在当前线程已绑定了数据库事务的情况下，当多次调用Mybatis的Mapper对象的方法时，获取到的是同一个Session对象。
		 */
		BaseDept dept = deptMapper.queryDeptSelectLines(8888);
		BaseDept cacheDept = deptMapper.queryDept(8888);
		Assert.isTrue(dept == cacheDept, "");

		// 由于参数值9999不符合condition的条件，因此查询结果没有被存储到缓存中
		BaseDept dept1 = deptMapper.queryDeptSelectLines(9999);
		BaseDept cacheDept1 = deptMapper.queryDept(9999);
		Assert.isTrue(dept1 != cacheDept1, "");

		// 由于参数值1111不满足@Cacheable的condition的条件，因此不会被CacheManager缓存起来，因此两次都会进行查询
		BaseLine line1 = lineMapper.queryLineSelectDept(1111);
		BaseLine line2 = lineMapper.queryLineSelectDept(1111);
		Assert.isTrue(line1 != line2, "");

		// 参数值3333满足了@Cacheable的condition的条件，因此会被CacheManager缓存，因此第二次执行会从缓存中获取数据
		BaseLine line3 = lineMapper.queryLineSelectDept(3333);
		BaseLine line4 = lineMapper.queryLineSelectDept(3333);
		Assert.isTrue(line3 == line4, "");

		// 满足@CacheEvict条件，将缓存中的数据全部清除
		lineMapper.queryLineSelectDept(-1);

		// 由于缓存中的数据已经被全部清除了，因此此时会重新进行查询
		BaseLine line5 = lineMapper.queryLineSelectDept(3333);
		BaseLine line6 = lineMapper.queryLineSelectDept(3333);
		Assert.isTrue(line4 != line5, "");
		Assert.isTrue(line5 == line6, "");
	}

}