package com.spring.testframe;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.commons.lang3.time.DateUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.format.Formatter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.spring.data.domain.BaseDept;

@RunWith(SpringRunner.class)
@ContextConfiguration
@TestPropertySource(properties = { "date=2017-10-20", "dateTime=${date} 20:30:11" })
@IfProfileValue(name = "user.name", value = "maziqiang")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD, hierarchyMode = HierarchyMode.CURRENT_LEVEL)
public class TestConversionService {

	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = true)
	static class ContextConfig {

		@Bean
		public DeptConverter deptConverter() {
			DeptConverter converter = new DeptConverter();
			return converter;
		}

		@Bean
		// 由于当前<bean>是singleton的，因此是在容器创建时就创建这个<bean>的实例，因此此时不能使用@TestPropertySource提供的资源
		public DateFormatter dateFormatter(@Value("yyyy-MM-dd kk:mm:ss") String datePattern) {
			DateFormatter dateFormatter = new DateFormatter(datePattern);
			return dateFormatter;
		}

		@Bean
		public ConverterTracer converterTracer() {
			return new ConverterTracer();
		}

		@Bean
		public FormattingConversionServiceFactoryBean conversionService(Set<Formatter<?>> converters) {
			FormattingConversionServiceFactoryBean conversionService = new FormattingConversionServiceFactoryBean();
			conversionService.setFormatters(converters);

			return conversionService;
		}
	}

	private static class DeptConverter implements Formatter<BaseDept> {

		@Override
		public String print(BaseDept dept, Locale locale) {
			StringJoiner joiner = new StringJoiner(",");
			joiner.add(dept.getDeptNo().toString());
			joiner.add(dept.getDeptCode());
			joiner.add(dept.getDeptName());
			return joiner.toString();
		}

		@Override
		public BaseDept parse(String text, Locale locale) throws ParseException {
			String[] splitted = text.split(",");

			BaseDept dept = new BaseDept(Integer.valueOf(splitted[0]), splitted[1]);
			dept.setDeptName(splitted[2]);

			return dept;
		}
	}

	@Aspect
	private static class ConverterTracer {

		@Pointcut("execution(public * org.springframework.format.Formatter.*(..)) && args(source,locale)")
		public void converterMethods(Object source, Locale locale) {
		}

		@Around(value = "converterMethods(source,locale)", argNames = "source,locale")
		public Object converterAroundCheck(ProceedingJoinPoint pjp, Object source, Locale locale) throws Throwable {
			locale = locale == null ? Locale.getDefault() : locale;
			Object[] params = { source, locale };
			Object result = pjp.proceed(params);
			return result;
		}
	}

	@Autowired
	private DeptConverter deptConverter;

	@Autowired
	private DateFormatter dateFormatter;

	@Autowired
	private ConversionService conversionService;

	@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	private Date date1;

	@Value("zhangsan,lisi,wangwu,zhaoliu")
	private String[] strArray;

	@Value("${dateTime}")
	private Date date2;

	@Test
	public void test1() throws ParseException {
		BaseDept dept = new BaseDept(40, "40N");
		dept.setDeptName("test_dept_123");

		String deptPrinted = deptConverter.print(dept, null);
		System.out.println(deptPrinted);

		BaseDept deptParsed = deptConverter.parse(deptPrinted, null);
		Assert.isTrue(dept.getDeptName().equals(deptParsed.getDeptName()), "公司名称不一致");
	}

	@Test
	public void test2() throws ParseException {
		Date now = DateUtils.truncate(new Date(), Calendar.SECOND);
		String printDate = dateFormatter.print(now, null);
		Date parsedDate = dateFormatter.parse(printDate, null);
		Assert.isTrue(now.equals(parsedDate), "时间不一致");
	}

	@Test
	public void test3() {
		Field date1Field = ReflectionUtils.findField(TestConversionService.class, "date1");
		TypeDescriptor date1Descriptor = new TypeDescriptor(date1Field);

		Date now = DateUtils.truncate(new Date(), Calendar.SECOND);
		String printDate = (String) conversionService.convert(now, date1Descriptor,
				TypeDescriptor.valueOf(String.class));

		Date parsedDate = (Date) conversionService.convert(printDate, TypeDescriptor.valueOf(String.class),
				date1Descriptor);
		Assert.isTrue(now.equals(parsedDate), "时间不一致");
	}

	@Test
	public void test4() {
		HttpStatus httpStatus = conversionService.convert("ACCEPTED", HttpStatus.class);
		System.out.println(httpStatus.value());

		HttpStatus firstStatus = conversionService.convert(0, HttpStatus.class);
		System.out.println(firstStatus.value());
	}

}
