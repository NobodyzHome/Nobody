package com.spring.web.converter;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.StringJoiner;

import org.springframework.format.Formatter;

import com.spring.data.domain.BaseDept;


public class BaseDeptFormatter implements Formatter<BaseDept> {

	@Override
	public String print(BaseDept dept, Locale locale) {
		StringJoiner joiner = new StringJoiner(",");
		joiner.add(dept.getDeptNo().toString());
		joiner.add(dept.getDeptName());
		joiner.add(DateFormat.getDateTimeInstance().format(dept.getModifyDate()));

		return joiner.toString();
	}

	@Override
	public BaseDept parse(String text, Locale locale) throws ParseException {
		String[] splitted = text.split(",");

		int deptNo = Integer.valueOf(splitted[0]);
		String deptName = splitted[1];
		Date modifyDate = DateFormat.getDateTimeInstance().parse(splitted[2]);

		BaseDept dept = new BaseDept();
		dept.setDeptNo(deptNo);
		dept.setDeptName(deptName);
		dept.setModifyDate(modifyDate);

		return dept;
	}

}
