package com.spring.test;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.ErrorMode;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.spring.data.dao.mapper.BaseDeptMapper;
import com.spring.data.dao.mapper.BaseLineMapper;
import com.spring.data.dao.mapper.BaseTerminalMapper;
import com.spring.data.domain.BaseDept;
import com.spring.data.domain.BaseLine;
import com.spring.data.domain.BaseTerminal;
import com.spring.data.service.BaseDeptService;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/container/busness-service.xml")
@ActiveProfiles({ "jingzhou", "test" })
@SqlConfig(errorMode = ErrorMode.FAIL_ON_ERROR, encoding = "UTF-8")
@TestPropertySource(properties = { "year=2017", "startTime=${year}-10-31 10:52:33", "endTime=${year}-11-01 12:30:59",
		"planDate=${year}-12-07", "lineNo=5" })
public class MybatisTest {

	@Value("${startTime}")
	@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	private Date startTime;

	@Value("${endTime}")
	@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	private Date endTime;

	@Value("${planDate}")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date planDate;

	@Value("${lineNo}")
	private Integer lineNo;

	@Autowired
	private BaseDeptMapper deptMapper;

	@Autowired
	private BaseLineMapper lineMapper;

	@Autowired
	private BaseTerminalMapper terminalMapper;

	@Autowired
	private BaseDeptService deptService;

	@BeforeTransaction
	public void beforeTransaction() {
		System.out.println("MybatisTest.beforeTransaction()");
	}

	@AfterTransaction
	public void afterTransaction() {
		System.out.println("MybatisTest.afterTransaction()");
	}

	@Test
	@Sql(statements = { "insert into base_dept values(1111,'aaaa','测试部a',6,11,CURRENT_TIMESTAMP,1)",
			"insert into base_dept values(2222,'bbbb','测试部b',6,11,CURRENT_TIMESTAMP,1)" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test1() {
		BaseDept dept = new BaseDept();
		dept.setDeptName("部");
		List<BaseDept> deptList = deptMapper.findByExample(dept);
		System.out.println(deptList);
	}

	/*
	 * 关于@Sql的一个使用细节
	 * 
	 * 如果@Sql的errorMode是CONTINUE_ON_ERROR，那么执行每一个sql语句发生错误后，会忽略该sql语句，
	 * 然后继续执行下一个sql语句，而不是后面的sql语句都不执行了。
	 * 
	 * 例如@Sql(statements={A,B,C},scripts="test.sql")，假设test.sql中的语句为D、E、F，
	 * 
	 * 那么如果errorMode是CONTINUE_ON_ERROR的话，执行A语句发生错误，也会继续执行B语句，只不过A语句的内容没有被保存到数据库。
	 * 依次类推，执行完所有语句后就提交事务。不会因为执行A语句失败，后面的B、C、D等语句就不执行了。
	 * 而如果errorMode是FAIL_ON_ERROR，那么当正常执行完A、B语句后，执行C语句时发生错误，那么后面的D、E、F就不会被执行，
	 * 并且整个事务会被回滚，因此A、B语句的执行内容也不会记录到数据库中。
	 */
	@Test
	@Transactional
	@Sql(config = @SqlConfig(errorMode = ErrorMode.CONTINUE_ON_ERROR, transactionMode = TransactionMode.ISOLATED), executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, statements = {
			"insert into base_dept values(1111,'1111','测试部1',6,11,CURRENT_TIMESTAMP,1)",
			"insert into base_dept values(2222,'2222','测试部2',6,11,CURRENT_TIMESTAMP,1)",
			"insert into base_dept values(3333,'3333','测试部3',6,1111,CURRENT_TIMESTAMP,1)" }, scripts = "classpath:database/sql/dml/data_dept.sql")
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test2() {
		test1();
		BaseDept example = new BaseDept();
		example.setDeptName("公司");
		List<BaseDept> deptList = deptMapper.findByExample(example);
		deptList.forEach(dept -> System.out.println(dept.getDeptName()));
	}

	@Test
	@Transactional(readOnly = true)
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test3() {
		BaseDept example = new BaseDept();
		List<BaseDept> deptList = deptMapper.findByExample(example);
		deptList.forEach(dept -> System.out.println(dept.getDeptName()));
	}

	@Test
	@Transactional
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test4() {
		BaseDept example = new BaseDept();
		example.setParent(new BaseDept(11, null));

		List<BaseDept> deptList = deptMapper.findByExample(example);
		deptList.forEach(dept -> System.out.println(dept.getDeptName()));

		example.setDeptName("公司");
		example.setIsRun(true);
		example.setDeptLevel(6);
		example.setModifyDate(new Date());

		deptList = deptMapper.findByExample(example);
	}

	@Test
	@Transactional
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test5() {
		BaseDept dept = deptMapper.findById(7777);
		Objects.requireNonNull(dept);
		do {
			System.out.println(dept.getDeptName());
			System.out.println(dept.toString());
			dept = dept.getParent();
		} while (Objects.nonNull(dept));
	}

	@SuppressWarnings("unused")
	@Test
	@Transactional
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test6() {
		BaseDept parent = new BaseDept(123, "123");
		parent.setDeptName("测试公司123");
		parent.setIsRun(false);
		parent.setDeptLevel(15);
		parent.setModifyDate(new Date());

		BaseDept child = new BaseDept(456, "456");
		child.setDeptName("测试公司456");
		child.setIsRun(true);
		child.setDeptLevel(1);
		child.setModifyDate(new Date());
		child.setParent(parent);

		MessageFormat message = new MessageFormat("已{0,choice,1#更新|2#插入|3#删除}了{1,number,#行}数据！");

		int affectedRows = deptMapper.insertByExample(parent);
		String prompt = message.format(ArrayUtils.toArray(2, affectedRows));
		System.out.println(prompt);

		affectedRows = deptMapper.insertByExample(child);
		System.out.println(prompt);

		BaseDept parent1 = deptMapper.findById(parent.getDeptNo());
		BaseDept child1 = deptMapper.findById(child.getDeptNo());

		Map<String, Object> updateValues = new HashMap<>();
		updateValues.put("dept_name", "公司哈哈" + parent1.getDeptNo());
		updateValues.put("dept_code", String.valueOf(parent1.getDeptNo() + 1213));

		Map<String, Object> conditions = new HashMap<>();
		conditions.put("dept_no", parent1.getDeptNo());
		conditions.put("modify_date", parent.getModifyDate());
		conditions.put("is_run", parent1.getIsRun());

		affectedRows = deptMapper.updateByParams(updateValues, conditions);

		prompt = message.format(ArrayUtils.toArray(1, affectedRows));
		System.out.println(prompt);

		BaseDept parent2 = deptMapper.findById(parent1.getDeptNo());
		System.out.println(parent2.getDeptName() + ":" + parent2.getDeptCode());

		affectedRows = deptMapper.deleteByParams(ArrayUtils.toArray(parent1.getDeptNo(), child.getDeptNo()), null, null,
				null);

		prompt = message.format(ArrayUtils.toArray(3, affectedRows));
		System.out.println(prompt);
	}

	@SuppressWarnings("unchecked")
	@Test
	@Transactional
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test7() {
		Map<String, Object> paramMap1 = new HashMap<>();
		paramMap1.put("startTime", startTime);
		paramMap1.put("endTime", endTime);

		deptMapper.callFunction(paramMap1);
		Integer interval = (Integer) paramMap1.get("intervalHours");

		MessageFormat message = new MessageFormat(
				"{0,date,yyyy-MM-dd kk:mm:ss}和{1,date,yyyy-MM-dd kk:mm:ss}之间的差距是{2}小时");

		String prompt = message.format(ArrayUtils.toArray(startTime, endTime, interval));
		System.out.println(prompt);

		Map<String, Object> paramMap2 = new HashMap<>();
		paramMap2.put("lineNo", lineNo);
		paramMap2.put("planDate", planDate);

		deptMapper.callProcedure(paramMap2);

		Integer shiftModel = (Integer) paramMap2.get("shiftModel");

		MessageFormat message1 = new MessageFormat("{0,number,#线路}在{1,date,yyyy-MM-dd}使用的模版是：{2}");
		prompt = message1.format(ArrayUtils.toArray(lineNo, planDate, shiftModel));
		System.out.println(prompt);
	}

	@Test
	@Transactional
	@IfProfileValue(name = "user.name", value = "zhangsan")
	@Sql(statements = {
			"insert into base_dept(dept_no,dept_code,dept_name,is_run,modify_date,dept_level,parent_dept_no) values(191,'191','191公司',0,sysdate,11,null)",
			"insert into base_dept(dept_no,dept_code,dept_name,is_run,modify_date,dept_level,parent_dept_no) values(192,'192','192公司',1,sysdate-1,3,191)" })
	public void test8() {
		BaseDept child = deptMapper.findById(192);
		BaseDept parent = child.getParent();

		System.out.println(child.getDeptName() + ":" + parent.getDeptName());
	}

	@Test
	@Transactional
	@IfProfileValue(name = "user.name", value = "zhangsan")
	@Sql(statements = {
			"insert into base_dept(dept_no,dept_code,dept_name,is_run,modify_date,dept_level,parent_dept_no) values(223,'223dept','223公司',0,CURRENT_TIMESTAMP,12,null)",
			"insert into base_dept(dept_no,dept_code,dept_name,is_run,modify_date,dept_level,parent_dept_no) values(224,'224dept','224公司',1,CURRENT_TIMESTAMP,5,223)" })
	@Sql(statements = {
			"insert into base_line(line_no,line_code,line_name,is_run,modify_time,dept_no) values(112,'112line','112线路',1,CURRENT_TIMESTAMP,224)",
			"insert into base_line(line_no,line_code,line_name,is_run,modify_time,dept_no) values(113,'113line','113线路',0,CURRENT_TIMESTAMP,223)" })
	public void test9() {
		BaseLine example = new BaseLine(112, "line", "线路");
		example.setIsRun(true);
		example.setDept(new BaseDept(224, null));
		BaseLine[] lineList1 = lineMapper.selectByExample(example);
		List<BaseLine> lineList2 = lineMapper.selectById(112, 113);

		Consumer<BaseLine> lineShower = line -> {
			line.equals(new Object());

			System.out.println(
					line.getLineName() + ":" + (Objects.isNull(line.getDept()) ? "无" : line.getDept().getDeptName()));

			Optional.ofNullable(line.getDept()).ifPresent(dept -> {
				System.out.println(StringUtils.center("公司等级结构情况【BEGIN】", 50, "="));
				BaseDept temp = dept;
				do {
					System.out.println(temp.getDeptName());
				} while ((temp = temp.getParent()) != null);
				System.out.println(StringUtils.center("公司等级结构情况【END】", 50, "="));
			});
		};

		Arrays.stream(lineList1).forEach(lineShower);
		lineList2.forEach(lineShower);
	}

	@Test
	@Transactional
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test10() {
		BaseLine line1 = lineMapper.testCache(3);
		BaseLine line2 = lineMapper.testCache(3);
		BaseLine line3 = lineMapper.testCache(3);
		BaseLine line4 = lineMapper.testCache(3);

		System.out.println(line1);
		System.out.println(line2);
		System.out.println(line3);
		System.out.println(line4);
	}

	@SuppressWarnings("unchecked")
	@Test
	@Transactional
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test11() {
		SpelExpressionParser parser = new SpelExpressionParser();
		Expression expression = parser.parseExpression(
				"{'line_no':1777,'line_code':'1777line','line_name':'1777线路','modify_time':new java.util.Date(),'is_run':false,'dept_no':1221}");
		Map<String, Object> conditions = (Map<String, Object>) expression.getValue(Map.class);

		lineMapper.insertByMap(conditions);

		List<BaseLine> lineList = lineMapper.selectById(Integer.valueOf(conditions.get("line_no").toString()));
		lineList.forEach(line -> System.out.println(line.getLineName()));

		BaseLine line = lineList.get(0);
		BaseLine lineCopy = new BaseLine(line.getLineNo(), line.getLineCode());
		lineCopy.setLineName("哈哈" + line.getLineName());
		lineCopy.setIsRun(true);
		lineCopy.setModifyDate(new Date());
		lineCopy.setDept(new BaseDept(21, null));

		lineMapper.updateByExample(lineCopy, conditions);

		List<BaseLine> lineList2 = lineMapper.selectById(line.getLineNo());
		lineList2.forEach(line1 -> System.out.println(line1.getLineName()));

		lineMapper.deleteById(lineList2.get(0).getLineNo());

		List<BaseLine> lineList3 = lineMapper.selectById(lineList2.get(0).getLineNo());
		System.out.println(lineList3.size());
	}

	@Test
	@Transactional
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test12() {
		Map<String, Object> paramMap1 = new HashMap<>();
		paramMap1.put("startTime", startTime);
		paramMap1.put("endTime", endTime);

		lineMapper.callFunction(paramMap1);

		System.out.println(paramMap1.get("cha"));

		Map<String, Object> paramMap2 = new HashMap<>();
		paramMap2.put("lineNo", 5);
		paramMap2.put("updown", 1);
		paramMap2.put("datetime", new Date());

		lineMapper.callProcedure(paramMap2);
		paramMap2.forEach((key, value) -> System.out.println(key + ":" + value));
	}

	@Test
	@Transactional
	@IfProfileValue(name = "user.name", value = "zhangsan")
	@Sql(config = @SqlConfig(errorMode = ErrorMode.CONTINUE_ON_ERROR), statements = {
			"insert into base_dept(dept_no,dept_code,dept_name,is_run,modify_date,parent_dept_no) values(113,'113','113公司',0,sysdate,null)",
			"insert into base_dept(dept_no,dept_code,dept_name,is_run,modify_date,parent_dept_no) values(114,'114','114公司',0,sysdate,113)" })
	@Sql(config = @SqlConfig(errorMode = ErrorMode.CONTINUE_ON_ERROR), statements = {
			"insert into base_line(line_no,line_code,line_name,is_run,modify_time,dept_no) values(211,'211','211线路',1,sysdate,114)",
			"insert into base_line(line_no,line_code,line_name,is_run,modify_time,dept_no) values(212,'212','212线路',1,sysdate,114)",
			"insert into base_line(line_no,line_code,line_name,is_run,modify_time,dept_no) values(213,'213','213线路',0,sysdate,114)",
			"insert into base_line(line_no,line_code,line_name,is_run,modify_time,dept_no) values(214,'214','214线路',0,sysdate,114)",
			"insert into base_line(line_no,line_code,line_name,is_run,modify_time,dept_no) values(215,'215','215线路',1,sysdate,113)",
			"insert into base_line(line_no,line_code,line_name,is_run,modify_time,dept_no) values(216,'216','216线路',1,sysdate,113)",
			"insert into base_line(line_no,line_code,line_name,is_run,modify_time,dept_no) values(217,'217','217线路',0,sysdate,113)" })
	@Rollback
	public void test13() {
		BaseDept dept1 = deptMapper.findById(113);
		dept1.toString();

		BaseDept dept2 = deptMapper.selectDeptWithLines(114);
		System.out.println(dept2);

		BaseDept dept3 = deptMapper.selectDeptWithLines(114);
		System.out.println(dept3);
	}

	@Test
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test14() {
		BaseTerminal terminal1 = terminalMapper.findById(124);
		System.out.println(terminal1);
		System.out.println(terminal1.getLine());

		BaseTerminal terminal2 = terminalMapper.findByIdWithLines(124);
		System.out.println(terminal2);
	}

	@Test
	@Transactional
	@IfProfileValue(name = "user.name", value = "zhangsan")
	public void test15() {
		BaseDept dept = deptMapper.deptWithAllInfo(1);

		Optional.ofNullable(dept).ifPresent(theDept -> {

			do {
				System.out.println("公司" + theDept.getDeptName() + "下的线路(共" + theDept.getLines().size() + "条)【开始】");

				theDept.getLines().forEach(line -> {
					System.out.println(line.getLineName() + "(" + line.getDept().getDeptName() + ")下的车辆(共"
							+ line.getTerminals().size() + "辆)【开始】");

					line.getTerminals().forEach(terminal -> {
						System.out.println(terminal.getTerminalCode() + "(" + terminal.getLine().getLineName() + "_"
								+ terminal.getLine().getDept().getDeptName() + ")");
					});

					System.out.println(line.getLineName() + "(" + line.getDept().getDeptName() + ")下的车辆【结束】");
				});

				System.out.println("公司" + theDept.getDeptName() + "下的线路【结束】");
			} while (Objects.nonNull((theDept = theDept.getParent())));
		});
	}

	@SuppressWarnings("unused")
	@Test
	@IfProfileValue(name = "user.name", value = "maziqiang")
	public void test16() {
		BaseDept dept1 = deptMapper.findById(1);
		// 此时会从二级缓存中获取数据（因为没有在@Test方法设置@Transactional，因次调用deptMapper的方法都是创建一个新的session，因此就避免了使用一级缓存）
		BaseDept dept2 = deptMapper.findById(1);

		List<BaseDept> deptList1 = deptMapper.findByExample(new BaseDept(1, null));
		List<BaseDept> deptList2 = deptMapper.findByExample(new BaseDept(1, null));

		List<BaseLine> lineList1 = lineMapper.selectById(new Integer[] { 1, 2, 3 });
		// 此时会从二级缓存中获取数据
		List<BaseLine> lineList2 = lineMapper.selectById(new Integer[] { 1, 2, 3 });
	}

	@Test
	@Transactional
	public void test17() {
		BaseDept dept = deptService.findById(1);
		System.out.println(dept.getDeptName());
	}
}