package com.spring.test.resource;

import java.util.Properties;

import org.springframework.test.annotation.ProfileValueSource;
import org.springframework.test.annotation.SystemProfileValueSource;
import org.springframework.util.StringUtils;

public class PropertiesProfileValueSource implements ProfileValueSource {

	private SystemProfileValueSource systemProfileValueSource = SystemProfileValueSource.getInstance();

	private Properties properties;

	@Override
	public String get(String key) {
		String result = properties.getProperty(key);
		return StringUtils.hasText(result) ? result : systemProfileValueSource.get(key);
	}

	public PropertiesProfileValueSource() {
		properties = new Properties();
		properties.put("hello", "你好");
		properties.put("world", "世界");
	}
}
