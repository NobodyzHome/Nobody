package com.springboot.data.domain;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
  如果没有在启动类中增加@EnableConfigurationProperties注解，这个类想要被装配成容器的bean
  ，然后用配置文件中student.age/student.name等属性的值来为这个bean的属性赋值，
  就需要在这个类上同时增加@Component和@ConfigurationProperties("student")注解。

  启动类上如果有@SpringBootApplication注解注解，SpringBoot则会在启动的时候扫描项目中的所有组件
  ，将他们装配到容器中，并用配置文件为这个bean的属性赋值。
  总结：
  启动类中没有@EnableConfigurationProperties注解时：
    Domain类需要：
        1.增加@Component注解，用于将该类装配到容器中
        2.增加@ConfigurationProperties注解，用于告诉容器使用配置文件中的属性为该bean的属性赋值
    启动类需要：
        1.增加@SpringBootApplication注解，容器会默认对本项目中的组件进行扫描，装配到容器中

  如果在启动类中加了@EnableConfigurationProperties注解，那么Domain类想被装配成bean，只需要在这个类上增加
  @ConfigurationProperties注解，容器会自动找到当前项目中的被@ConfigurationProperties注解的类，然后将他们装配成
  bean。相当于给这些类加了@Component注解。

  启动类中设置了@EnableConfigurationProperties注解时：
    Domain类需要：
        1.增加@ConfigurationProperties注解，用于告诉容器使用配置文件中的属性为该bean的属性赋值

    启动类需要：
        1.增加@SpringBootApplication注解，容器会默认对本项目中的组件进行扫描，装配到容器中
        2.增加@EnableConfigurationProperties注解，用于搜索项目中所有被@ConfigurationProperties注解的类，将他们装配成bean

  总之：当容器装配一个bean后，发现这个bean的类上有@ConfigurationProperties注解，容器就会自动用配置文件中的对应属性的配置，来为这个bean的属性来赋值。

  注意：
    如果启动类中增加了@SpringBootApplication注解，相当于在启动类中增加了,@EnableAutoConfiguration、@Configuration、@ComponentScan注解

  使用@ConfigurationProperties("student")为Student这个bean的属性注入值的方式是：
  1.由于ConfigurationProperties("student")中是student前缀，在给Student类的id属性赋值时，就是从配置文件中获取key为student.id的值
  2.在给Student类的address属性的postCode属性赋值时，就是从配置文件中获取key为student.address.postCode属性赋值
*/
@ConfigurationProperties("student")
// 如果需要spring boot对创建的这个bean的属性值进行校验，则需要加上@Validated注解
//@Validated
// 加上@Profile注解代表，只有在stu或student这两个profile被激活时，当前Student组件类才会被激活，才有可能被装配到容器中。
// 该配置相当于<beans profile="stu,student">
@Profile({"stu", "student"})
@Component
public class Student {

    @Id
    @Range(min = 100, max = 2000)
    private Integer id;

    @Range(min = 10, max = 15)
    private Integer age;
    private String name;
    @NotBlank
    private String sex;

    private List<String> alias;
    private Integer[] sanWei;

    // 虽然不对address属性增加@Valid注解也可以对address属性进行验证，但是好的习惯还是增加@Valid注解
    @Valid
    @NotNull
    private Address address;

    @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
    private Date birthDay;

    @NumberFormat(pattern = "第#,###位学生")
    private Integer identifyNo;

    private Map<String, Integer> friendsNo;
    private Map<String, Student> friends;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<String> getAlias() {
        return alias;
    }

    public void setAlias(List<String> alias) {
        this.alias = alias;
    }

    public Integer[] getSanWei() {
        return sanWei;
    }

    public void setSanWei(Integer[] sanWei) {
        this.sanWei = sanWei;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Date getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

    public Integer getIdentifyNo() {
        return identifyNo;
    }

    public void setIdentifyNo(Integer identifyNo) {
        this.identifyNo = identifyNo;
    }

    public Map<String, Integer> getFriendsNo() {
        return friendsNo;
    }

    public void setFriendsNo(Map<String, Integer> friendsNo) {
        this.friendsNo = friendsNo;
    }

    public Map<String, Student> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, Student> friends) {
        this.friends = friends;
    }

    static class Address {
        private Integer postCode;
        @NotEmpty
        private String province;
        private String city;
        private String county;
        private String address;

        private Set<Integer> otherPostCodes;
        private String[] tags;

        public Integer getPostCode() {
            return postCode;
        }

        public void setPostCode(Integer postCode) {
            this.postCode = postCode;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCounty() {
            return county;
        }

        public void setCounty(String county) {
            this.county = county;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Set<Integer> getOtherPostCodes() {
            return otherPostCodes;
        }

        public void setOtherPostCodes(Set<Integer> otherPostCodes) {
            this.otherPostCodes = otherPostCodes;
        }

        public String[] getTags() {
            return tags;
        }

        public void setTags(String[] tags) {
            this.tags = tags;
        }
    }
}
