package com.spring.redis.listener;

import java.io.Serializable;
import java.util.Map;

// 可以看到，这个接口和spring的MessageListener接口没有关系
public interface MessageDelegate {

	/*
	 * 以下都是消息处理方法，第一个参数决定了可以接受的消息对象（也就是消息被反序列化后的对象）的类型，第二个参数是消息的频道名称
	 * 
	 * 那么消息对象由谁创建呢？就是MessageAdapterListener，它使用<redis:listener>指定的serializer，
	 * 将消息反序列化成对应对象，然后调用hanleMessage方法
	 */
	// 该方法处理的消息对象的类型必须是String以下
	void handleMessage(String message, String channel);

	// 该方法处理的消息对象的类型必须是Map以下
	void handleMessage(Map message);

	void handleMessage(byte[] message);

	// 该方法处理的消息对象的类型必须是Serializable以下
	void handleMessage(Serializable message);

	// pass the channel/pattern as well
	void handleMessage(Serializable message, String channel);
}
