package com.spring.rabbitmq.consumer;

public interface LogRecord {

    void handleMsg(byte[] msg, String name);

    void consume(String bindingKey, String name);
}
