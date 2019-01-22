package com.rabbitmq.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.spring.rabbitmq.consumer.TopicConsumer;
import org.junit.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TopicTest {

    private static TopicConsumer topicConsumer;

    @BeforeClass
    public static void getConnection() throws IOException, TimeoutException {
        topicConsumer = new TopicConsumer();
    }

    @AfterClass
    public static void close() {
        topicConsumer.close();
    }

    @After
    public void sleep(){
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(10));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1() throws IOException, TimeoutException {
        topicConsumer.consume("queue1", "topic-log", "*.orange.*");
    }

    @Test
    public void test2() throws IOException, TimeoutException {
        topicConsumer.consume("queue2", "topic-log", "*.*.rabbit");
    }

    @Test
    public void test3() throws IOException, TimeoutException {
        topicConsumer.consume("queue3", "topic-log", "lazy.#");
    }

    @Test
    public void test4() throws IOException, TimeoutException {
        topicConsumer.consume("queue4", "topic-log", "#");
    }

    @Test
    public void test5() throws IOException, TimeoutException {
        topicConsumer.consume("queue5", "topic-log", "*");
    }

    @Test
    public void test6() throws IOException, TimeoutException {
        topicConsumer.consume("queue6", "topic-log", "lazy.orange.rabbit.baby");
    }

    @Test
    public void testSend() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare("topic-log", "topic");
            channel.basicPublish("topic-log", "test.orange.rabbit", null, "哈哈士大夫".getBytes());
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
