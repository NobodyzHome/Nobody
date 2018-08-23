package com.spring.redis.listener;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class Subscriber1 {

	public void handleMessage(String message, String channel) {
		System.out.println("Subscriber.handleMessage1()");
	}

	public void handleMessage(Map message) {
		System.out.println("Subscriber.handleMessage2()");
	}

	public void handleMessage(byte[] message) {
		System.out.println("Subscriber.handleMessage3()");
	}

	public void handleMessage(Serializable message) {
		System.out.println("Subscriber.handleMessage4()");
	}

	public void handleMessage(Date date, String channel) {
		System.out.println("Subscriber.handleMessage6()");
	}

	public void handleMessage1(Serializable message, String channel) {
		System.out.println("Subscriber.handleMessage5()");
	}
}
