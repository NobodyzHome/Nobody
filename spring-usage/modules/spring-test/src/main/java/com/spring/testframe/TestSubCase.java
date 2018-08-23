package com.spring.testframe;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;

import com.spring.data.domain.BaseDept;
import com.spring.data.service.BaseDeptService;

@ActiveProfiles("jiayuguan")
public class TestSubCase extends TestParentCase {

	@Autowired
	private BaseDeptService deptService;

	@Test
	public void subTest1() {
		List<BaseDept> deptList = deptService.queryDepts("%公司%");
		deptList.forEach(dept -> System.out.println(dept));
	}

	@Test
	@IfProfileValue(name = "user.name", values = { "zhangsan", "maziqiang123" })
	public void subTest2() {
		while (true) {

		}
	}

}