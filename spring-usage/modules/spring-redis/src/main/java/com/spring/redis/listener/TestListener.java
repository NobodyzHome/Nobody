package com.spring.redis.listener;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

// 可以看到，这个类和spring的MessageListenerAdapter类没有关系
public class TestListener implements MessageDelegate {

	// 下发的消息是哪个类型，就会调用哪个方法。取决于监听器方法的第一个参数的类型
	public void handleMessage(String message, String channel) {
		System.out.println("TestListener.handleMessage1()");
	}

	/*
	 * 话题订阅者的方法名可以一样（例如本例中方法名都是handleMessage），spring会挨个尝试判断每一个方法，
	 * 用发送消息的参数值的类型和handleMessage方法的第一个参数的类型做判断，如果第一个参数的类型和发送消息的参数值的类型相兼容，
	 * 就调用这个方法，否则继续判断下一个handleMessage方法。
	 * 
	 * 但有一点要注意的是：假如有两个handleMessage方法，其中一个handleMessage方法的第一个参数是Map类型，
	 * 另一个方法的第一个参数是Object类型。假如MessageListenerAdapter反序列化消息得到的消息对象是Map类型，
	 * 可以看到这两个handleMessage方法的第一个参数都可以处理Map类型的对象。但是由于jdk本身的原因，
	 * 使用反射获取一个类的所有名为handleMessage方法的顺序是不固定的，因此如果spring获得的handleMessage(Object)
	 * 方法在前，就只会调用该方法了，不会调用handleMessage(Map)了，反之亦然。
	 * 
	 * 另外，我们从订阅者方法的第一个参数能观察到： handleMessage(Map)方法代表消息对象必须是Map类型以下的，
	 * handleMessage(String)方法代表消息对象必须是String类型以下的。
	 */
	public void handleMessage(Map message) {
		System.out.println("TestListener.handleMessage2()");
	}

	public void handleMessage(byte[] message) {
		System.out.println("TestListener.handleMessage3()");
	}

	public void handleMessage(Date message, String channel) {
		System.out.println("TestListener.handleMessage4()");
	}

	public void handleMessage(Serializable message) {
		System.out.println("TestListener.handleMessage4()");
	}

	// pass the channel/pattern as well
	public void handleMessage(Serializable message, String channel) {
		System.out.println("TestListener.handleMessage5()");
	}
	
	public void handleMessage1(Serializable message, String channel) {
		System.out.println("TestListener.handleMessage6()");
	}

	public void handleMessage2(Serializable message, String channel) {
		System.out.println("TestListener.handleMessage7()");
	}
}
