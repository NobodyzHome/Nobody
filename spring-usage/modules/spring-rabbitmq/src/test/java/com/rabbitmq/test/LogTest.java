package com.rabbitmq.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.spring.rabbitmq.consumer.LogRecord;
import com.spring.rabbitmq.consumer.LogSend;
import com.spring.rabbitmq.consumer.SysErrLogRecord;
import com.spring.rabbitmq.consumer.SysInfLogRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LogTest {

    private static Connection connection;
    private static LogRecord sysoutLog;
    private static LogRecord syserrLog;
    private static LogSend logSend;

    @BeforeClass
    public static void getConnection() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connection = connectionFactory.newConnection();

        sysoutLog = new SysInfLogRecord(connection);
        syserrLog = new SysErrLogRecord(connection);
        logSend = new LogSend(connection);
    }

    @AfterClass
    public static void closeConnection() throws IOException {
        syserrLog = null;
        syserrLog = null;
        logSend = null;
        connection.close();
    }

    @Test
    public void test1() throws IOException {
        sysoutLog.consume("color_black", "black");
        sysoutLog.consume("color_orange", "orange");
        syserrLog.consume("color_red", "red");
        Channel channel = connection.createChannel();
        channel.queueBind("red", "direct-log", "color_orange");
        channel.queueBind("orange", "direct-log", "color_red");
        channel.queueBind("black", "direct-log", "color_red");
        logSend.send("direct-log", "color_red", "张三", "李四", "哇哈哈", "游侠", "赵武", "马六", "王琦");
        logSend.send("direct-log", "color_orange", "李磊", "韩梅梅", "玛丽", "奥", "鲁奇");

        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(10));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {
        sysoutLog.consume("color_black", "black");

        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(10));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3() {
        sysoutLog.consume("color_orange", "orange");

        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(10));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test4() {
        syserrLog.consume("color_red", "red");

        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(10));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test5() throws IOException {
        Channel channel = connection.createChannel();
        channel.queueBind("red", "direct-log", "color_orange");
        channel.queueBind("black", "direct-log", "color_red");
        channel.queueBind("orange", "direct-log", "color_red");
        channel.queueBind("orange", "direct-log", "color_black");
    }

    @Test
    public void testSend() {
        logSend.send("direct-log", "color_orange", "小刚", "效力", "灼灼", "哟士大夫", "的风格");
        logSend.send("direct-log", "color_black", "微软", "据哦", "厉害厉害", "儿童");
        logSend.send("direct-log", "color_red", "one", "two", "three", "four");
    }
}
