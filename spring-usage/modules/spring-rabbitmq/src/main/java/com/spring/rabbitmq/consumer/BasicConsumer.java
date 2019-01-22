package com.spring.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BasicConsumer {

    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 3.Message durability
        // 使用Message acknowledgment机制仍不能保证消息不会丢失。因为默认情况下，rabbitmq将队列、消息等内容存储到内存中，如果rabbitMq服务器挂机，就会导致内存中的队列、消息等信息都丢失了。
        // 解决这种问题的办法就是：同时使队列和队列中的消息都持久化到本地硬盘，我们通过声明队列时，传入durable=ture，将队列变为持久化的。这样，即使服务器挂掉，当服务器恢复运行后，也能保留住之前的队列、消息等信息。
        // 在发送消息时，可以告诉rabbitmq来持久化这个消息
        boolean durable = true;
        channel.queueDeclare("hello", durable, false, false, null);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody());

            for (char c : message.toCharArray()) {
                if (c == '.') {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            System.out.println(" [x] Received '" + message + "'");
        };

        // 1.rourd-robbin 将队列中的消息公平的发送给每一个和这个队列连接的consumer
        // 默认情况下，队列会把消息公平的发给每一个consumer，保证每个consumer接收的消息数据一致，不会考虑每个consumer执行任务的时间。这种消息分发方式被称为rourd-robbin。

        // 2.Message acknowledgment 消息确认机制，确保将消息发给consumer后，如果consumer挂掉，会导致消息丢失
        // 当一个队列收到消息后，如果consumer接收消息后并没有给队列返回ack=true，那么队列会继续保存这个消息，不会删除，并将消息再转发给下一个consumer。这种方式保证了消息不会丢，我们称为Message acknowledgment。
        // 默认情况下，Message acknowledgment是开启的，consumer消费消息时是没有给队列返回ack=true的，需要在接收消息时手动进行ack响应
        // 如果在消费消息时，设置autoAck=true，那么消费消息时会自动返回ack=true。
        boolean autoAck = false;

        // 4.Fair dispatch
        // 默认情况下，rabbitmq是将消息盲目地平均分给每一个消费者。例如任务1给消费者1，任务2给消费者2，任务3给消费者1，任务4给消费者2。
        // 这样，有一个问题假如任务1和任务3的工作很多，任务2和任务4的工作很少，导致消费者1始终很忙，消费者2始终很闲。
        // 为了打破这个问题，rabbitmq可以设置basicQos，告知rabbitmq每次给这个消费者分配任务时，最多不要超过2个。待这个消费者返回ack内容后，再给消费者下发任务，但给这个消费者的任务最多不会超过2个。
        // 这样，rabbitmq就不会盲目的下发任务了，我每次给消费者两个，谁消费完了我就再给他下发任务。这样，就不会导致有的消费者一直获得的是工作多的任务，而有的则一直获得少的。
        channel.basicQos(2);

        channel.basicConsume("hello", autoAck, deliverCallback, consumerTag -> {
        });

    }
}
