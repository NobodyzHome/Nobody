package com.spring.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class LogSend {

    private Connection connection;

    public LogSend(Connection connection) {
        this.connection = connection;
    }

    public Channel getChannel() throws IOException {
        return connection.createChannel();
    }

    public void send(String exchange, String routingKey,String... messages) {
        try (Channel channel = getChannel()) {
            channel.exchangeDeclare(exchange, "direct");

            Arrays.stream(messages).forEach(message -> {
                try {
                    channel.basicPublish(exchange, routingKey, null, message.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
