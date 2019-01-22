package com.spring.rabbitmq.consumer;

import com.rabbitmq.client.Channel;

import java.io.IOException;

public abstract class LogRecordAdapter implements LogRecord {

    public abstract Channel getChannel() throws IOException;

    @Override
    public void consume(String bindingKey, String name) {
        try {
            Channel channel = getChannel();
            String queue = channel.queueDeclare(name, false, false, true, null).getQueue();
            String exchange = "direct-log";
            // 声明一个direct类型的exchange。在sub/pub模式中，exchange和queue相连，exchange一旦收到任何信息后，就会把消息发给所有相连的queue。
            // 但是如果我们期望只发给一部分queue，fanout类型的exchange就没有办法实现了，此时我们可以使用direct类型的exchange，让exchange区分要将信息发送给哪个queue。
            // 但是如果要让exchange进行区分，仅仅和queue相连是不够的，还需要在这个连接上设置一个bindingKey。与此同时，向这个exchange发送一个消息时，也需要给出一个routingKey，
            // 这样，direct类型的exchange判断routingKey和某个bindingKey相同，就会发送给使用这个bindingKey连接的queue。
            channel.exchangeDeclare(exchange, "direct");
            channel.queueBind(queue, exchange, bindingKey);
            channel.basicQos(2);

            channel.basicConsume(queue, false, (consumerTag, message) -> {
                handleMsg(message.getBody(), name);
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
            }, System.err::println);
            System.out.println(String.format("已消费队列 -- %s", name));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
