package com.spring.rabbitmq.sender;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class LogEmiter {

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare("logs", "fanout");
            String[] messages = {"你好", "你", "123", "测试121", "超级无敌霹雳", "今天是晴天", "天"};
            Arrays.stream(messages).forEach(msg -> {
                try {
                    channel.basicPublish("logs", "", null, msg.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }
}
