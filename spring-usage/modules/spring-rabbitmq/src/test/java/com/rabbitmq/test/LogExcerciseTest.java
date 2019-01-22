package com.rabbitmq.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.spring.rabbitmq.consumer.LogRecordExcercise;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LogExcerciseTest {

    private String exchangeType_fanout = "fanout";
    private String exchange_fanout = String.format("%s-exchange", exchangeType_fanout);
    private String exchangeType_direct = "direct";
    private String exchange_direct = String.format("%s-exchange", exchangeType_direct);

    @Test
    public void testFanout1() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(exchange_fanout, exchangeType_fanout);
            channel.queueDeclare("queue1", false, false, true, null);
            channel.queueDeclare("queue2", false, false, true, null);
            channel.queueBind("queue1", exchange_fanout, "");
            channel.queueBind("queue2", exchange_fanout, "");

            channel.basicConsume("queue1", false, (consumerTag, message) -> {
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                System.out.println(String.format("[%s]已收到消息：%s", "queue1", new String(message.getBody())));
            }, System.err::println);

            channel.basicConsume("queue2", false, (consumerTag, message) -> {
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                System.out.println(String.format("[%s]已收到消息：%s", "queue2", new String(message.getBody())));
            }, System.err::println);

            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(10));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogRecordExcercise logRecordExcercise = new LogRecordExcercise(new String[]{"queue1", "queue2"}, "fanout-exchange");
        logRecordExcercise.excerciseFanout();
    }

    @Test
    public void testFanout2() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(exchange_fanout, "fanout");
            channel.queueDeclare("queue2", false, false, true, null);
            channel.queueDeclare("queue3", false, false, true, null);
            channel.queueBind("queue2", exchange_fanout, "");
            channel.queueBind("queue3", exchange_fanout, "");

            channel.basicConsume("queue2", false, (consumerTag, message) -> {
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                System.out.println(String.format("[%s]已收到消息：%s", "queue2", new String(message.getBody())));
            }, System.err::println);

            channel.basicConsume("queue3", false, (consumerTag, message) -> {
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                System.out.println(String.format("[%s]已收到消息：%s", "queue3", new String(message.getBody())));
            }, System.err::println);

            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(10));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDirect1() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(exchange_direct, exchangeType_direct);
            channel.queueDeclare("queue1", false, false, true, null);
            channel.queueBind("queue1", exchange_direct, "info");

            channel.basicConsume("queue1", false, (consumerTag, message) -> {
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                System.out.println(String.format("[%s]已收到消息：%s", "queue1", new String(message.getBody())));
            }, System.err::println);

            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(10));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDirect2() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(exchange_direct, exchangeType_direct);
            channel.queueDeclare("queue2", false, false, true, null);
            channel.queueBind("queue1", exchange_direct, "debug");
            channel.queueBind("queue2", exchange_direct, "info");
            channel.queueBind("queue2", exchange_direct, "debug");
            channel.queueBind("queue2", exchange_direct, "error");

            channel.basicConsume("queue1", false, (consumerTag, message) -> {
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                System.out.println(String.format("[%s]已收到消息：%s", "queue1", new String(message.getBody())));
            }, System.err::println);

            channel.basicConsume("queue2", false, (consumerTag, message) -> {
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                System.out.println(String.format("[%s]已收到消息：%s", "queue2", new String(message.getBody())));
            }, System.err::println);

            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(10));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDirect3() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(exchange_direct, exchangeType_direct);
            channel.queueDeclare("queue3", false, false, true, null);
            channel.queueBind("queue3", exchange_direct, "error");
            channel.queueBind("queue1", exchange_direct, "error");
            channel.queueBind("queue2", exchange_direct, "warn");

            channel.basicConsume("queue3", false, (consumerTag, message) -> {
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                System.out.println(String.format("[%s]已收到消息：%s", "queue3", new String(message.getBody())));
            }, System.err::println);

            channel.basicConsume("queue2", false, (consumerTag, message) -> {
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                System.out.println(String.format("[%s]已收到消息：%s", "queue2", new String(message.getBody())));
            }, System.err::println);

            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(10));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSend() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(exchange_fanout, exchangeType_fanout);
            channel.basicPublish(exchange_fanout, "", null, "哈哈".getBytes());
            channel.basicPublish(exchange_fanout, "", null, "张三".getBytes());
            channel.basicPublish(exchange_fanout, "", null, "李四".getBytes());
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSend1() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(exchange_direct, exchangeType_direct);
            channel.basicPublish(exchange_direct, "warn", null, "哈哈".getBytes());
            channel.basicPublish(exchange_direct, "info", null, "张三".getBytes());
            channel.basicPublish(exchange_direct, "debug", null, "李四".getBytes());
            channel.basicPublish(exchange_direct, "error", null, "拉拉".getBytes());
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
