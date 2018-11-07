package com.spring.es;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerTest {

    // log4j2的启动是在调用LoggerFactory.getLogger()方法后启动的，不需要其他额外处理，只需要在classpath下增加log4j2.xml这个配置即可
    private static Logger logger1 = LoggerFactory.getLogger(LoggerTest.class);
    // org.springframework.test就是分类名，也就是PatternLayout中c%输出的内容
    private static Logger logger2 = LoggerFactory.getLogger("org.springframework.test");
    private static Logger logger3 = LoggerFactory.getLogger("test");
    // 没有匹配的Logger，使用RootLogger进行输出
    private static Logger logger4 = LoggerFactory.getLogger("sdfasdfsfs");

    @Test
    public void test1() {
        logger1.debug("test123");
        logger1.info("test456");
        logger1.warn("test789");
        logger1.error("test11111");

        logger2.debug("test22222");
        logger2.error("test33333");

        logger3.trace("test44444");
        logger3.error("test55555");

        logger4.trace("test66666");
        logger4.debug("test77777");
        logger4.error("test88888");
    }
}
