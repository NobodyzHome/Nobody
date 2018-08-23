package com.spring.testframe;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.validator.constraints.Length;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;

@RunWith(SpringRunner.class)
@ContextConfiguration
@IfProfileValue(name = "user.name", values = { "zhangsan", "lisi", "maziqiang" })
@TestPropertySource(properties = { "deptNo=123", "deptCode=No:${deptNo}", "deptName=${deptNo}公司" })
@ActiveProfiles("validateMethod")
public class TestBindingResult {

	@Configuration
	static class ContextConfiguration {

		@Bean
		public MessageSource messageSource(
				@Value("bindingResult/error,bindingResult/message,bindingResult/field") String... basenames) {
			ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
			messageSource.setBasenames(basenames);
			messageSource.setUseCodeAsDefaultMessage(false);
			messageSource.setFallbackToSystemLocale(true);
			messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());

			return messageSource;
		}

		@Bean
		public OptionalValidatorFactoryBean validator(MessageSource messageSource) {
			OptionalValidatorFactoryBean validator = new OptionalValidatorFactoryBean();
			validator.setValidationMessageSource(messageSource);

			return validator;
		}

		@Bean
		public FormattingConversionServiceFactoryBean conversionService() {
			FormattingConversionServiceFactoryBean conversionService = new FormattingConversionServiceFactoryBean();
			conversionService.setRegisterDefaultFormatters(true);

			return conversionService;
		}

		@Bean
		public Dept dept(@Value("第3号") @NumberFormat(pattern = "第#,###号") Integer id, @Value("No:3123") String code,
				@Value("3123公") String name,
				@Value("2019-10-05 09:30") @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm") Date modifyDate) {
			Dept dept = new Dept();
			dept.id = id;
			dept.code = code;
			dept.name = name;
			dept.modifyDate = modifyDate;

			return dept;
		}

		@Bean(autowire = Autowire.BY_TYPE)
		@Profile("validateMethod")
		public static MethodValidationPostProcessor methodValidationPostProcessor() {
			MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
			return methodValidationPostProcessor;
		}

	}

	@Validated
	static class Dept {

		// 以下介绍了jsr-303的注解中，message属性的值的三种设置方式
		// 1.设置message属性的值是一个占位符，占位符名称为Min.id
		@Min(value = 50, message = "{Min.id}")
		@NumberFormat(pattern = "第#,###号")
		Integer id;

		// 2.没有设置message属性的值，那么默认的message属性的值也是个占位符，占位符名称为org.hibernate.validator.constraints.Length.message
		@Length(min = 2, max = 6)
		String code;

		// 3.设置message属性的值是一个常量字符串，而非一个占位符
		@Pattern(regexp = "\\d+公司", message = "公司名称不符合指定的样式")
		String name;

		@Past
		@NotNull
		@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm")
		public Date modifyDate;

		public Integer getId() {
			return id;
		}

		public String getCode() {
			return code;
		}

		public String getName() {
			return name;
		}

		public Date getModifyDate() {
			return modifyDate;
		}

		public String validateParam(@Length(min = 3, max = 10, message = "{Length.words}") String words,
				@Size(min = 5, max = 12) Integer... nums) {
			String newWords = words + Arrays.stream(nums).collect(Collectors.summingInt(Integer::valueOf));
			return newWords;
		}

		@Past(message = "{Past.returning}")
		public Date validateReturning(Date date, int offset) {
			return DateUtils.addDays(date, offset);
		}

		@Max(value = 1000, message = "{Max.returning}")
		public Integer validateAll(@Min(value = 30, message = "{Min.param}") Integer number1,
				@Max(value = 100, message = "{Max.param}") Integer number2) {
			return number1 * number2;
		}

	}

	@Value("#{{'deptNo':'${deptNo}','deptCode':'${deptCode}','deptName':'${deptName}'}}")
	private Map<String, String> beanMap;

	@Autowired
	private Dept dept;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private Validator validator;

	@SuppressWarnings("unchecked")
	@Test
	@Ignore
	public void testBindingResult() {
		/*
		 * 1.手动使用BindingResult创建FieldError对象的方式
		 * 
		 * BindingResult用于存储验证结果FieldError，
		 * 同时BindingResult的rejectValue方法也能用于创建FieldError对象。
		 * 
		 * 当由BindingResult通过rejectValue方法来创建FieldError对象时，这个FieldError对象的：
		 * 1.code属性由调用rejectValue方法传入的errorCode参数的值来生成的，共会生成四个code值。
		 * 2.args、defaultMessage属性由调用rejectValue方法的对应参数决定的。
		 */
		BindingResult mapBindingResult = new MapBindingResult(beanMap, "userMap");
		if (!beanMap.get("deptCode").equals("test")) {
			mapBindingResult.rejectValue("deptCode", "WrongValue");
		}

		BindingResult directFieldBindingResult = new DirectFieldBindingResult(dept, "basicDept");
		if (dept.id < 50) {
			directFieldBindingResult.rejectValue("id", "TooSmall", ArrayUtils.toArray(new Date(), 5123),
					"This value is too small!");
		}

		BeanPropertyBindingResult beanPropertyBindingResult = new BeanPropertyBindingResult(dept, "theDept");
		if (!dept.getName().contains("公司")) {
			beanPropertyBindingResult.rejectValue("name", "InvalidPattern", "这个值的样式不合法");
		}

		Consumer<FieldError> fieldErrorShower = fieldError -> System.out
				.println(messageSource.getMessage(fieldError, null));

		mapBindingResult.getFieldErrors().forEach(fieldErrorShower);
		directFieldBindingResult.getFieldErrors().forEach(fieldErrorShower);
		beanPropertyBindingResult.getFieldErrors().forEach(fieldErrorShower);
	}

	@Test
	public void testBindingResultUseValidator() {
		// 2.使用Validator来验证bean实例的属性值，并根据验证结果自动创建FieldError对象

		/*
		 * 如果使用ValidationUtils的invokeValidator方法，来让Validator根据一个实例中的属性上的jsr-
		 * 303上的注解来校验对应的属性值的话。当某一个属性的值不满足这个属性上的jsr-303注解的要求时，那么
		 * 此时不会抛出异常，而是创建一个FieldError对象，来记录这次的校验。例如date属性的值是2019-10-20，而
		 * date属性上有@Past注解，由于不满足@Past的期望，因此会创建一个FieldError对象。
		 * 
		 * 这种方式创建的FieldError对象，其code、args、defaultMessage属性的值是由spring提供的，
		 * 而不是我们自己给出的。
		 * 
		 * 这里只说下这个FieldError对象的defaultMessage属性，假设验证的注解是@Length(min=2,max=6,
		 * message={Length.message})，
		 * spring会使用Validator关联的MessageSource来获取占位符“Length.message”对应的值，
		 * 设置为FieldError对象的defaultMessage属性。
		 */
		BindingResult mapBindingResult = new MapBindingResult(beanMap, "userMap");
		DirectFieldBindingResult directFieldBindingResult = new DirectFieldBindingResult(dept, "basicDept");
		BeanPropertyBindingResult beanPropertyBindingResult = new BeanPropertyBindingResult(dept, "theDept");

		ValidationUtils.invokeValidator(validator, beanMap, mapBindingResult);
		ValidationUtils.invokeValidator(validator, dept, directFieldBindingResult);
		ValidationUtils.invokeValidator(validator, dept, beanPropertyBindingResult);

		Consumer<FieldError> fieldErrorShower = fieldError -> System.out
				.println(messageSource.getMessage(fieldError, Locale.getDefault()));

		mapBindingResult.getFieldErrors().forEach(fieldErrorShower);
		directFieldBindingResult.getFieldErrors().forEach(fieldErrorShower);
		beanPropertyBindingResult.getFieldErrors().forEach(fieldErrorShower);
	}

	@Test
	public void testMethodValidation() {
		/*
		 * 使用MethodValidationPostProcessor来验证传入方法的参数值和方法的返回值，也是使用Validator来验证。
		 * 但与BindingResult不同的是，
		 * 当使用MethodValidationPostProcessor验证出传入方法的参数的值或方法返回值不符合要求时，会直接报错，
		 * 而不是记录这个不符合的情况。报错的内容中会包括jsr-303注解中的message属性对应的内容。
		 */
		System.out.println(dept.validateParam("你好世界", 3, 7, 9, 11, 22, 5));
		System.out.println(dept.validateReturning(new Date(), -2));
		System.out.println(dept.validateAll(33, 40));
	}
}