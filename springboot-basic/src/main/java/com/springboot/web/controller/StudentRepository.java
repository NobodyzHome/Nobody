package com.springboot.web.controller;

import com.springboot.data.domain.Student;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.stream.Stream;

public interface StudentRepository extends MongoRepository<Student, Integer> {

    Stream<Student> findByAgeGreaterThan(int age);
}
