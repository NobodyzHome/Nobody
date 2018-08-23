package com.spring.testframe;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration
// 注意：@TestPropertySource中也可以继续使用占位符，例如：startDate=2017${dateSplitor}9${dateSplitor}10
@TestPropertySource(properties = { "dateSplitor=-", "startDate=2017${dateSplitor}9${dateSplitor}10",
		"endDate=2017${dateSplitor}7${dateSplitor}25", "jieSuan=26" })
@ActiveProfiles("enableConversion")
@IfProfileValue(name = "user.name", value = "maziqiang")
public class TestDate {

	@Configuration
	static class ContextConfig {
		@Bean
		@Profile("enableConversion")
		public FormattingConversionServiceFactoryBean conversionService() {
			FormattingConversionServiceFactoryBean conversionService = new FormattingConversionServiceFactoryBean();
			conversionService.setRegisterDefaultFormatters(true);
			conversionService.setFormatters(Collections.singleton(new DateFormatter("yyyy-MM-dd")));

			return conversionService;
		}
	}

	@Value("${startDate}")
	private Date startDate;

	@Value("${endDate}")
	private Date endDate;

	@Value("#{'${endDate}'.split('${dateSplitor}')[0]+'${dateSplitor}'+'${endDate}'.split('${dateSplitor}')[1]+'${dateSplitor}'+'${jieSuan}'}")
	private Date curJieSuanDate;

	@Test
	@IfProfileValue(name = "user.name", values = { "zhangsan", "lisi", "maziqiang" })
	public void test() {
		Date realJieSuan;
		if (DateUtils.truncatedCompareTo(endDate, curJieSuanDate, Calendar.DAY_OF_MONTH) >= 0) {
			realJieSuan = curJieSuanDate;
		} else {
			realJieSuan = DateUtils.addMonths(curJieSuanDate, -1);
		}
		System.out.println(DateFormat.getDateInstance().format(realJieSuan));
	}
}
