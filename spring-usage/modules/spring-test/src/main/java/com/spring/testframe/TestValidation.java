package com.spring.testframe;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.validation.Validator;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class TestValidation {

	@Configuration
	static class ContextConfiguration {

		@Bean
		public MessageSource messageSource(@Value("validation/excercise/error") String baseName) {
			ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
			messageSource.setBasename(baseName);
			return messageSource;
		}

		@Bean
		public Validator validator(MessageSource messageSource) {
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

		@Bean(autowire = Autowire.BY_TYPE)
		public static BeanValidationPostProcessor beanValidation() {
			BeanValidationPostProcessor beanValidationPostProcessor = new BeanValidationPostProcessor();
			return beanValidationPostProcessor;
		}

		@Bean
		public static MethodValidationPostProcessor methodValidation(Validator validator) {
			MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
			methodValidationPostProcessor.setValidator(validator);
			return methodValidationPostProcessor;
		}

		@Bean
		@DependsOn("conversionService")
		public TestBean testBean(@Value("No:1,213") @NumberFormat(pattern = "No:#,###") Integer id,
				@Value("13公司") String name, @Value("No:1998") String code,
				@Value("2017-10-05 08:30:22") @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss") Date birthDay,
				@Value("5000.03") @NumberFormat(style = Style.CURRENCY) Double salary) {

			TestBean bean = new TestBean();
			bean.id = id;
			bean.name = name;
			bean.code = code;
			bean.birthDay = birthDay;
			bean.salary = salary;
			return bean;
		}
	}

	@Validated
	static class TestBean {

		@Range(min = 1000, max = 2000, message = "id范围不正确")
		private Integer id;

		@Length(min = 3, max = 10, message = "{Length.name}")
		@NotBlank
		@Pattern(regexp = "\\d+公司", message = "公司名称的样式错误")
		private String name;

		@Length(min = 4, max = 7)
		private String code;

		@Past
		@NotNull(message = "{NotNull.birthDay}")
		private Date birthDay;

		@DecimalMax(value = "5000.05", message = "{DecimalMax.salary}")
		@DecimalMin(value = "2500.1", message = "{DecimalMin.salary}")
		private Double salary;

		public String validateParam(@Range(min = 10, max = 20) int num1, @Min(30) int num2) {
			return String.valueOf(num1 + num2);
		}

		@Size(min = 5, max = 20)
		public List<Integer> validateResult(Integer... numbers) {
			return Arrays.asList(numbers);
		}

		@Max(30)
		@Min(15)
		public int validateAll(@Min(3) int num1, @Max(10) int num2, @Range(min = 2, max = 6) int num3) {
			return num1 + num2 + num3;
		}
	}

	@Autowired
	private TestBean testBean;

	@Test
	public void test1() {
		String result = testBean.validateParam(11, 30);
		System.out.println(result);
		testBean.validateResult(1, 2, 3, 7, 10, 11);
		testBean.validateAll(5, 7, 3);
	}
}
