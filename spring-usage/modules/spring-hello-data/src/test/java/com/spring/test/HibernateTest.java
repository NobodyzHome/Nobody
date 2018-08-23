package com.spring.test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.ErrorMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.spring.data.domain.BaseDept;
import com.spring.data.domain.BaseLine;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/container/busness-service.xml")
@Transactional("hibernateTransactionManager")
@ActiveProfiles("embedded")
@SqlConfig(encoding = "UTF-8", errorMode = ErrorMode.CONTINUE_ON_ERROR)
public class HibernateTest {

	@Autowired
	private SessionFactory sessionFactory;

	@Test
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test1() {
		Session session = sessionFactory.getCurrentSession();
		BaseDept dept = session.get(BaseDept.class, 1);

		Optional.ofNullable(dept).ifPresent(theDept -> {
			do {
				System.out.println(theDept.getDeptName() + ","
						+ (theDept.getParent() == null ? "无父公司" : theDept.getParent().getDeptName()));
				Optional.ofNullable(theDept.getLineSet())
						.ifPresent(lineSet -> lineSet.forEach(line -> System.out.println(line.getLineName() + ","
								+ line.getDept().getDeptName() + "," + (line.getDept().getParent() == null ? "无父公司"
										: line.getDept().getParent().getDeptName()))));
			} while (Objects.nonNull((theDept = theDept.getParent())));
		});
	}

	@Test
	@Sql(statements = {
			"insert into base_dept(dept_no,dept_code,dept_name,is_run,modify_date,dept_level,parent_dept_no) "
					+ "values(555,'555dept','555公司',1,sysdate,13,null)" })
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test2() {
		Session session = sessionFactory.getCurrentSession();

		BaseDept dept = session.get(BaseDept.class, 555);
		System.out.println(dept.getDeptName());
		dept.setDeptName(dept.getDeptName() + "哈哈");
		dept.setIsRun(true);

		session.flush();
		session.clear();

		BaseDept dept1 = session.get(BaseDept.class, 555);
		System.out.println(dept1.getDeptName());
	}

	@Test
	@Sql(statements = {
			"insert into base_dept(dept_no,dept_code,dept_name,is_run,modify_date,dept_level,parent_dept_no) "
					+ "values(555,'555dept','555公司',1,sysdate,13,null)" })
	@IfProfileValue(name = "user.name", value = "maziqiang")
	public void test3() {
		Session session = sessionFactory.openSession();
		BaseDept dept1 = session.get(BaseDept.class, 555);
		String deptRegion = "dept";
		showSecondCacheData(deptRegion);
		session.close();

		Session newSession = sessionFactory.openSession();
		Transaction tx = newSession.beginTransaction();
		BaseDept dept2 = newSession.get(BaseDept.class, 555);
		showSecondCacheData(deptRegion);
		dept2.setDeptName("test");
		tx.commit();
		newSession.close();

		Session newSession1 = sessionFactory.openSession();
		showSecondCacheData(deptRegion);
		BaseDept dept3 = newSession1.get(BaseDept.class, 555);
		newSession1.close();

		Session newSession2 = sessionFactory.openSession();
		showSecondCacheData(deptRegion);
		BaseDept dept4 = newSession2.get(BaseDept.class, 555);
		newSession2.close();

		String lineRegion = "line";
		String lineQueryRegion = "lineQuery";

		Session newSession3 = sessionFactory.openSession();
		Query<BaseLine> lineQuery = newSession3.createQuery("from BaseLine", BaseLine.class);
		lineQuery.setCacheable(true);
		lineQuery.setCacheRegion(lineQueryRegion);
		List<BaseLine> lineList = lineQuery.list();

		showSecondCacheData(lineRegion);
		showSecondCacheData(lineQueryRegion);
		newSession3.close();

		Session newSession4 = sessionFactory.openSession();
		lineList.forEach(line -> {
			// 此时都是从二级缓存中获取BaseLine对象，不会查询数据库
			BaseLine lineSearched = newSession4.get(BaseLine.class, line.getLineNo());
			System.out.println(lineSearched.getLineName());
		});

		Query<BaseLine> lineQuery1 = newSession4.createQuery("from BaseLine", BaseLine.class);
		lineQuery1.setCacheable(true).setCacheRegion(lineQueryRegion);

		// 此时是从二级缓存中获取lineQuery1的查询结果，不会查询数据库
		List<BaseLine> lineList1 = lineQuery1.list();

	}

	private void showSecondCacheData(String region) {
		SecondLevelCacheStatistics secondLevelCacheStatistics = sessionFactory.getStatistics()
				.getSecondLevelCacheStatistics(region);
		Map map = secondLevelCacheStatistics.getEntries();

		System.out.println(map);
	}
}
