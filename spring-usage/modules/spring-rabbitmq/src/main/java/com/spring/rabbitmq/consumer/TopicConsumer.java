package com.spring.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TopicConsumer {

    private Connection connection;

    public TopicConsumer() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connection = connectionFactory.newConnection();
    }

    public void hanleMsg(byte[] msg) {
        System.out.println("已获取内容：" + new String(msg));
    }

    /**
     * 在讲direct类型的exchange时我们说过，可以通过bindingKey和routingKey来判断要给和该exchange连接的哪些queue发送消息。
     * 但是direct类型的exchange也有一定的缺陷：一个routingKey必须和bindingKey完全一致，才会发送到那个queue里。
     * 如果我想把一系列的routingKey都匹配到一个bindingKey里，那么direct类型的exchange就没办法实现了，这时我们可以使用topic类型的exchange。
     * 对于topic类型的exchange，它和queue相连的bindingKey可以使用一些通配符。当发送消息时，如果routingKey匹配了bindingKey的规则，就把消息发送到对应的队列中。
     * <p>
     * topic类型的bindingKey的规则如下：
     * #代表0个或多个单词；*代表一个单词；
     * <p>
     * 举例：
     * 如果bindingKey是*.range.*，那么满足条件的routingsKey有test.range.abc、girl.range.haha，不满足条件的有range(因为*代表必须有一个单词)、test.range、range.haha
     * 如果bindingKey是#.lazy.*，那么满足条件的是lazy.test、haha.lazy.range、lala.wawa.haha.lazy.user，不满足条件的有lazy、test.lazy.wawa.haha
     * 如果bindingKey是#，那么所有routingKey都满足条件
     * 如果bindingKey是*，那么所有一个单词的routingKey都满足条件，例如：test、haha、wawa、gaga、user，不满足条件的有test.haha、haha.user、gaga.lasa.next
     * 如果bindingKey不含*或#，例如test.range.user.stat，那么此时就和direct类型的exchange一样了，routingKey只有也是test.range.user.stat才会有匹配
     */
    public void consume(String queue, String exchange, String bindingKey) throws IOException, TimeoutException {
        Channel channel = connection.createChannel();
        channel.queueDeclare(queue, false, false, true, null);
        channel.exchangeDeclare(exchange, "topic");
        channel.queueBind(queue, exchange, bindingKey);
        channel.close();

        Channel channel1 = connection.createChannel();
        channel1.basicConsume(queue, false, (consumerTag, message) -> {
            channel1.basicAck(message.getEnvelope().getDeliveryTag(), false);
            hanleMsg(message.getBody());
        }, System.err::println);
        System.out.println("已消费队列：" + queue + "，bindingKey：" + bindingKey);
    }

    public void close() {
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
