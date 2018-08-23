package com.spring.testframe;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class TestMessageSource {

	@Configuration
	@EnableAspectJAutoProxy
	static class ContextConfiguration {

		@Bean("messageSource")
		public ResourceBundleMessageSource resourceBundleMessgeSource(
				@Value("messages/i18n/error,messages/i18n/field,messages/i18n/info") String... baseNames) {
			ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
			messageSource.setBasenames(baseNames);
			messageSource.setFallbackToSystemLocale(true);
			messageSource.setUseCodeAsDefaultMessage(false);
			return messageSource;
		}

		@Bean
		public MessageSourceTracer messageSourceTracer() {
			return new MessageSourceTracer();
		}

	}

	@Aspect
	public static class MessageSourceTracer {
		@Pointcut("execution(public * org.springframework.context.MessageSource.getMessage(..)) && args(..,locale)")
		public void getMessageMethods(Locale locale) {
		}

		@Around(value = "getMessageMethods(locale)", argNames = "locale")
		public Object arountAdvice(ProceedingJoinPoint pjp, Locale locale) throws Throwable {
			Object[] args = pjp.getArgs();
			args[args.length - 1] = Objects.isNull(locale) ? Locale.UK : locale;

			return pjp.proceed(args);
		}
	}

	@Autowired
	private MessageSource messageSource;

	@SuppressWarnings("unchecked")
	@Test
	@Ignore
	public void test() {
		String message1 = messageSource.getMessage("user.account.balance", ArrayUtils.toArray(999), null);
		System.out.println(message1);

		String message2 = messageSource.getMessage("user.account.balance.percent", ArrayUtils.toArray(0.37), null);
		System.out.println(message2);

		String message3 = messageSource.getMessage("error.file.notFount",
				ArrayUtils.toArray("DEBUG", "myfile.txt", new Date()), null);
		System.out.println(message3);

		String message4 = messageSource.getMessage("error.file.inused", ArrayUtils.toArray("hello.jpg"), null);
		System.out.println(message4);

		String message5 = messageSource.getMessage("user.account.balance", ArrayUtils.toArray(8000), null);
		System.out.println(message5);

		String message6 = messageSource.getMessage("user.account.balance.percent", ArrayUtils.toArray(0.87), null);
		System.out.println(message6);
	}

	@SuppressWarnings("unchecked")
	@Test
	@Ignore
	public void testDefaultMessage() {
		/*
		 * 可以看到，使用MessageSource的getMessage方法可以传入三个参数，即code,args和defaultMessage。
		 * 其中defaultMessage中如果设置了“变量”，那么也会从args参数中获取变量对应的值。
		 */
		String code = "noCode"; // MessageSource中肯定没有该code对应的值
		Object[] args = ArrayUtils.toArray(new Date(), 200, 30, 0.21);
		String defaultMessage = "今天是{0,date,yyyy-MM-dd},你的积分是{1,number,[#]},你的钱是{2,number,currency},你的百分比是"
				+ "{3,number,percent}";
		String message = messageSource.getMessage(code, args, defaultMessage, Locale.getDefault());
		System.out.println(message);

		// 以下是常规的code参数对应的值中如果包含“变量”（例如今天是{0,number,currency}），那么会从args参数中获取变量的值
		String messageUseCode = messageSource.getMessage("user.account.balance", ArrayUtils.toArray(8000), null);
		System.out.println(messageUseCode);
	}

	@Test
	@Ignore
	public void testUseCodeAsDefaultMessage() {
		String message1 = messageSource.getMessage("sdfsdff", null, null);
		System.out.println(message1);

		String message2 = messageSource.getMessage("error.file.inused", null, null);
		System.out.println(message2);
	}

	@Test
	public void testProblem() {
		String message = messageSource.getMessage(null, ArrayUtils.toArray(new Date(), 31231, "test"),
				"{0,date,yyyy-MM-dd} I'm the happy bird! {1,number,No:#,###} {2}", null);

		System.out.println(message);
	}

}
