package com.spring.testframe;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.ErrorMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.spring.data.dao.BaseRoleLineMapper;
import com.spring.data.domain.BaseRoleLine;
import com.spring.test.resource.PropertiesProfileValueSource;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/test/containers/busness-excercise.xml")
@ActiveProfiles("runDefault")
@ProfileValueSourceConfiguration(value = PropertiesProfileValueSource.class)
@IfProfileValue(name = "hello", value = "你好")
@SqlConfig(encoding = "UTF-8", errorMode = ErrorMode.CONTINUE_ON_ERROR)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS, hierarchyMode = HierarchyMode.CURRENT_LEVEL)
public class TestExcerciseAll2 {

	@Autowired
	private BaseRoleLineMapper roleLineMapper;

	@BeforeTransaction
	public void beforeTx() {
		System.out.println("TestExcerciseAll2.beforeTx()");
	}

	@AfterTransaction
	public void afterTx() {
		System.out.println("TestExcerciseAll2.afterTx()");
	}

	@Test
	@Transactional
	public void test1() {
		List<BaseRoleLine> roleLineList = roleLineMapper.findByLine(5, true);
		showRoleListList(roleLineList);
	}

	@Test
	@Transactional
	@IfProfileValue(name = "world", values = { "时节", "世界", "使节" })
	@Sql(statements = { "insert into base_role_line values(1998,1,1,sysdate)",
			"insert into base_role_line values(1998,2,1,sysdate)",
			"insert into base_role_line values(1998,3,1,sysdate)",
			"insert into base_role_line values(1998,4,0,sysdate)",
			"insert into base_role_line values(1998,5,0,sysdate)" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(statements = { "insert into base_role values(1998,'测试角色','测试',1,sysdate,9,null)" })
	@Rollback
	public void test2() {
		List<BaseRoleLine> roleLineList = roleLineMapper.findByRole(1998, true);
		showRoleListList(roleLineList);
	}

	@Test
	public void test3() {
		BaseRoleLine roleLine = roleLineMapper.fineByRoleLine(1, 9, true);
		showRoleLine(roleLine);
	}

	@Test
	@Transactional
	public void test4() {
		List<BaseRoleLine> roleLineList = roleLineMapper.findByOperate(true);
		showRoleListList(roleLineList);
	}

	@Value("2017-08-27")
	private Date date;

	@Test
	@Transactional
	@IfProfileValue(name = "user.name", value = "maziqiang")
	public void test5() {
		List<BaseRoleLine> roleLineList = roleLineMapper.findByModifyDate(date);
		showRoleListList(roleLineList);
	}

	@Test
	@DirtiesContext(methodMode = MethodMode.BEFORE_METHOD, hierarchyMode = HierarchyMode.CURRENT_LEVEL)
	public void test6() {
		List<BaseRoleLine> roleLineList1 = roleLineMapper.findByLine(5, true);
		List<BaseRoleLine> roleLineList2 = roleLineMapper.findByLine(5, true);

		Assert.isTrue(roleLineList1 == roleLineList2, "");
		
		List<BaseRoleLine> roleLineList3 = roleLineMapper.findByLine(11, true);
		List<BaseRoleLine> roleLineList4 = roleLineMapper.findByLine(11, true);
		
		Assert.isTrue(roleLineList3 != roleLineList4, "");
	}

	private void showRoleListList(List<BaseRoleLine> roleLineList) {
		roleLineList.forEach(roleLine -> {
			showRoleLine(roleLine);
		});
	}

	private void showRoleLine(BaseRoleLine roleLine) {
		String lineName = roleLine.getLine().getLineName();
		String roleName = roleLine.getRole().getRoleName();
		String modifyDate = DateFormat.getDateTimeInstance().format(roleLine.getModifyDate());

		System.out.println(lineName + ":" + roleName + ":" + modifyDate);
	}
}