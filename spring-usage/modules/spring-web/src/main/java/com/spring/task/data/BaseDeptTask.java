package com.spring.task.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.spring.data.domain.BaseDept;
import com.spring.data.service.BaseDeptService;

public class BaseDeptTask {

	@Autowired
	private BaseDeptService deptService;

	@Scheduled(fixedDelay = 10000)
	public void deptQueryTask() {
		BaseDept condition = new BaseDept();
		condition.setIsRun(false);

		List<BaseDept> deptList = deptService.queryDeptList(condition);

		System.out.println("显示运营公司开始");
		deptList.forEach(dept -> System.out.println(dept.getDeptName()));
		System.out.println("显示运营公司结束");
	}

	public void deptQueryTask1() {
		BaseDept condition = new BaseDept();
		condition.setDeptName("公司");

		List<BaseDept> deptList = deptService.queryDeptList(condition);

		System.out.println("显示【公司】开始");
		deptList.forEach(dept -> System.out.println(dept.getDeptName()));
		System.out.println("显示【公司】结束");
	}
}
