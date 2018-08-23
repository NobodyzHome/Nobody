package com.spring.data.domain;

import java.text.DateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date> {

	@Override
	public Date unmarshal(String v) throws Exception {
		return DateFormat.getDateTimeInstance().parse(v);
	}

	@Override
	public String marshal(Date v) throws Exception {
		return DateFormat.getDateTimeInstance().format(v);
	}

}
