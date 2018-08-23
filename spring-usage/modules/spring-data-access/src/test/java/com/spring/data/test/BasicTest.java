package com.spring.data.test;

import java.util.Arrays;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import com.spring.data.domain.BaseDept;
import com.spring.data.service.BaseDeptService;

public class BasicTest {

	@Test
	@Ignore
	public void test1() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext()) {
			ConfigurableEnvironment environment = context.getEnvironment();
			environment.addActiveProfile("test");
			context.setConfigLocation("spring/data-access.xml");
			context.refresh();

			BaseDeptService deptService = context.getBean(BaseDeptService.class);
			BaseDept condition = new BaseDept();
			condition.setDeptName("%公司%");

			BaseDept[] depts = deptService.queryDept(condition);
			Arrays.stream(depts)
					.forEach(dept -> System.out.println(dept.getDeptName() + "(" + dept.getDeptCode() + ")"));

			BaseDept deptToInsert = new BaseDept();
			deptToInsert.setDeptCode("testDept");
			deptToInsert.setDeptName("测试公司");
			deptToInsert.setIsRun(false);
			deptToInsert.setModifyDate(new Date());

			deptService.insertDept(deptToInsert);
			System.out.println(deptToInsert.getDeptNo());

			BaseDept deptToUpdate = new BaseDept();
			deptToUpdate.setDeptCode(deptToInsert.getDeptCode() + " modified");
			deptToUpdate.setDeptName(deptToInsert.getDeptName() + " modified");
			deptService.updateDept(deptToUpdate, deptToInsert);
			System.out.println(deptToUpdate.getDeptName());

			deptService.deleteDept(new BaseDept(deptToInsert.getDeptNo(), null));
		}
	}
	
}
