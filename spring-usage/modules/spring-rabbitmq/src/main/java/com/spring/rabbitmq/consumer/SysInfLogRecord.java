package com.spring.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

public class SysInfLogRecord extends LogRecordAdapter {

    private Connection connection;

    public SysInfLogRecord(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public Channel getChannel() throws IOException {
        return getConnection().createChannel();
    }

    @Override
    public void handleMsg(byte[] msg, String name) {
        String message = new String(msg);
        System.out.println(String.format("%s消费了消息：【%s】", name, message));
    }
}
