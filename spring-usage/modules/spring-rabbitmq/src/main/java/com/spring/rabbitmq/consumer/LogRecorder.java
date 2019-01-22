package com.spring.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LogRecorder {

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

             /*
                 rabbitmq的核心理念和exchange
                 在BasicConsumer和BasicSender的教程中，消费者消费一个指定名称的队列的消息，生产者往一个指定名称的队列里生产消息。这时队列的名称是有意义的，生产者和消费者要知道的是相同的队列名称。
                 但rabbitmq的核心思想是生产者不直接和队列相连，生产者甚至不知道生产的消息要被存到哪个队列中。取而代之的，生产者只和exchange相连，生产者仅把生产的消息发给exchange，
                 由exchange决定：1.是否要把消息发给队列 2.是否要发给指定的某个队列 3.是否要发给多个队列等等。这些规则是由exchange的类型决定。
                 因此，生产者不需要知道要给哪个名称的队列发送消息，也就是说生产者就不再需要知道队列的名称了

                 The core idea in the messaging model in RabbitMQ is that the producer never sends any messages directly to a queue. Actually, quite often the producer doesn't even know
                 if a message will be delivered to any queue at all.
                 Instead, the producer can only send messages to an exchange. An exchange is a very simple thing. On one side it receives messages from producers and the other side it pushes them to queues.
                 The exchange must know exactly what to do with a message it receives. Should it be appended to a particular queue? Should it be appended to many queues? Or should it get discarded.
                 The rules for that are defined by the exchange type.
             */

            // Temporary queues 临时队列
            // 由于生产者已经不需要知道队列名称了，只有消费者需要，那么消费者定义队列时，往往可以使用一个随机名称的临时队列
            // 我们可以使用channel.queueDeclare()来生成一个非持久的（non-durable）、排他的（exclusive）、自动删除的（autodelete）、拥有随机名称的（with a generated name）队列。
            // 注意：临时队列会随着创建这个队列的信道关闭而被删除，因此，如果不想临时队列被删除，信道就要一直连接着rabbitmq
            String queueName = channel.queueDeclare().getQueue();
            // 定义一个exchange，这个exchange的类型是fanout，这种类型的exchange在收到消息后，会无条件的发给所有和他相连的queue。
            channel.exchangeDeclare("logs", "fanout");
            // 定义这个exchange和哪个queue相连，由于fanout类型的exchange会无条件的把消息发送给相连的队列，因此这里就不需要传入routingKey了。
            channel.queueBind(queueName, "logs", "");

            channel.basicQos(2);
             /*
                basicConsume是非阻塞的，也就是说执行完订阅方法后，当前线程不会变成阻塞状态，一直等待消息。而是继续执行后续代码。
                当收到消息后，是在一个新的线程里执行消息处理，而不是当前线程。
                由于我们每次启动的时候是生成一个临时队列，并消费这个临时队列。因此，每次启动时消费的队列是不同的。可是如果多个consumer消费同一个队列的话，那么队列会把消息逐一分发
                ，但是不会给每个consumer都下发相同的消息。因此，在rabbitmq里，所谓的pub/sub模式，并不是多个消费者消费同一个队列，而是每个消费者消费不同的队列，这些队列和一个fanout类型的exchange相连
                ，当exchange在收到消息后，会将消息发送给每一个和它相连队列。
             */
            channel.basicConsume(queueName, false, (consumerTag, message) -> {
                String msg = new String(message.getBody());
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(msg.length()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                    System.out.println(String.format("已接收并处理完消息：【%s】", msg));
                }
            }, System.err::println);
            try {
                // 正是由于basicConsume方法不会阻塞，会继续后续代码，因此为了不让主线程走完，需要手动加一个阻塞。
                Thread.sleep(TimeUnit.MINUTES.toMillis(5));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
