package com.spring.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LogRecordExcercise {

    private String[] queueNames;
    private String[] bindingKeys;
    private String exchange;

    public LogRecordExcercise(String[] queueNames, String exchange, String... bindingKeys) {
        this.queueNames = queueNames;
        this.bindingKeys = bindingKeys;
        this.exchange = exchange;
    }

    public void excerciseFanout() {
        ConnectionFactory connectionFactory = new ConnectionFactory();

        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(exchange, "fanout");
            for (int index = 0, length = queueNames.length; index < length; index++) {
                String queue = queueNames[index];
                channel.queueDeclare(queue, false, false, true, null);
                channel.queueBind(queue, exchange, "");
                channel.basicConsume(queue, false, (consumerTag, message) -> {
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                    System.out.println(String.format("已收到内容：%s", new String(message.getBody())));
                }, System.err::println);

                System.out.println("已消费队列：" + queue);
            }

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

    public void excerciseDirect() {

    }

    public void excerciseTopic() {

    }
}
