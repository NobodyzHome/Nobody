package com.springboot;

import com.springboot.data.domain.*;
import org.apache.catalina.startup.UserConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/*
   如果增加了@SpringBootApplication注解，相当于在启动类上增加了以下三个注解：
    1.@Configuration。那么这个启动类中可以定义@Bean方法
    2.@ComponentScan。那么会自动扫描并装配这个项目中的所有组件
    3.@EnableAutoConfiguration。那么会根据当前项目引用的jar包来决定容器的配置（这个spring-boot的核心）
 */
@SpringBootApplication
/*
    @EnableConfigurationProperties会找到指定的类型，将他们装配到容器中，
    然后根据这个类型上的@ConfigurationProperties，使用对应的配置文件中的内容，给该bean的属性进行赋值
*/
@EnableConfigurationProperties({Student.class, BaseDept.class, BaseLine.class
        , BaseTerminal.class, BaseEmployee.class, Teacher.class, Book.class})

//@PropertySource中提供的配置文件必须是固定路径，不能包含通配符
@PropertySource({"classpath:config/terminal.properties"})
public class MyAppConfig {

    /*
        @ConfigurationProperties注解也可以注解在@Bean方法上，其主要目的是为了对第三方的类的属性进行赋值。
        因为对于第三方的类，你无法控制，不能在那个类上加上@ConfigurationProperties注解。

        这就和使用<tx-advise>是一样的，对于第三方的类，不能在类上加@Transational注解，就从外部使用配置AOP的形式，
        来为第三方的类的<bean>生成代理。
     */
    @Bean
    @ConfigurationProperties("config")
    public UserConfig userConfig() {
        return new UserConfig();
    }

    public static void main(String[] args) {
        SpringApplication.run(MyAppConfig.class, args);
    }
}
