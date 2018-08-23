package com.spring.web.converter;

import java.text.ParseException;
import java.util.Locale;
import java.util.StringJoiner;

import org.springframework.format.Formatter;

import com.spring.data.domain.BaseDept;

public class BaseDeptFormatter implements Formatter<BaseDept> {

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
