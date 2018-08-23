package com.spring.redis.listener;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import com.spring.redis.data.domain.BaseDept;

public class Subscriber {

	public void handleMessage(String message, String channel) {
		System.out.println("Subscriber.handleMessage(String,String):" + message + "__" + channel);

	}

	public void handleMessage(Map message) {
		System.out.println("Subscriber.handleMessage(Map):" + message);
	}

	public void handleMessage(byte[] message) {
		System.out.println("Subscriber.handleMessage(byte[]):" + message);
	}

	// public void handleMessage(Serializable message) {
	// System.out.println("Subscriber.handleMessage(Serializable)");
	// }
	//
	// public void handleMessage(Serializable message, String channel) {
	// System.out.println("Subscriber.handleMessage5(Serializable,String)");
	// }

	public void handleMessage(Date date, String channel) {
		System.out.println("Subscriber.handleMessage(Date,String):" + DateFormat.getDateTimeInstance().format(date)
				+ "__" + channel);
	}

	public void handleMessage(BaseDept dept, String channel) {
		System.out.println("Subscriber.handleMessage(BaseDept,String):" + dept.getDeptName() + "__" + channel);
	}

}
