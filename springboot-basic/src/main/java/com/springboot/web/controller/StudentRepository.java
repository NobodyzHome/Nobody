package com.springboot.web.controller;

import com.springboot.data.domain.Student;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface StudentRepository extends MongoRepository<Student, Integer> {

    Stream<Student> findByAgeGreaterThan(int age);

    List<Student> findByAgeBetween(int start, int end);

    Set<Student> findBySexIgnoreCase(String sex);
}
