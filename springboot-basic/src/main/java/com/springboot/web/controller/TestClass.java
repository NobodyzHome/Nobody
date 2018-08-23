package com.springboot.web.controller;


import com.springboot.data.domain.Student;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:mongo.xml")
@EnableMongoRepositories
public class TestClass {
    @Autowired
    private StudentRepository studentRepository;

    @Test
    public void test1() {
        Student student = new Student();
        student.setId(66);
        student.setAge(15);
        student.setName("wahaha");
        Student save = studentRepository.save(student);
        List<Student> all = studentRepository.findAll();

        Stream<Student> byAgeGreaterThan = studentRepository.findByAgeGreaterThan(12);
        byAgeGreaterThan.forEach(student1 -> System.out.println(student1.getName()));
        System.out.println(all);
    }

    @Test
    public void test2() {

    }
}
