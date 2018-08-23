package com.springboot.web.controller;

import com.springboot.data.domain.*;
import org.apache.catalina.startup.UserConfig;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.StringJoiner;

@RestController
@RequestMapping("/basic")
public class BasicController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired(required = false)
    private Student student;

    @Autowired(required = false)
    private BaseDept baseDept;

    @Autowired(required = false)
    private BaseLine baseLine;

    @Autowired(required = false)
    private BaseTerminal baseTerminal;

    @Autowired(required = false)
    private BaseEmployee baseEmployee;

    @Autowired(required = false)
    private StudentRepository studentRepository;

    @Autowired(required = false)
    private UserConfig userConfig;

    @Autowired(required = false)
    private Teacher teacher;

    @Autowired(required = false)
    private Book book;

    @GetMapping("/hello/{words}")
    public String hello(@PathVariable String words) {
        return words;
    }

    @GetMapping("/student/{age}/{name}/{sex}")
    public Student hello(Student student) {
        stringRedisTemplate.opsForValue().set("stuName", student.getName());
        mongoTemplate.save(student);
        return student;
    }

    @GetMapping("/redis")
    public String name() {
        String stuName = stringRedisTemplate.opsForValue().get("stuName");
        return stuName;
    }

    @GetMapping("/mongo/student/{name}")
    public List<Document> student(@PathVariable String name) {
        BasicQuery query = new BasicQuery("{name:'" + name + "'}", "{age:1,sex:1}");
        List<Student> all = studentRepository.findAll();

        List<Document> students = mongoTemplate.find(query, Document.class, "student");
        return students;
    }

    @GetMapping("/properties")
    public String prop(@Value("${name}") String name, @Value("${random.long}") Long num, @Value("${salary}") Integer salary, @Value("${cmdLine}") String fromCommandLine) {
        StringJoiner joiner = new StringJoiner(",");
        String contents = joiner.add(name).add(num.toString()).add(salary.toString()).add(fromCommandLine).toString();
        return contents;
    }

    @GetMapping("/showStudent")
    public Student showStudent() {
        return student;
    }

    @GetMapping("/showDept")
    public BaseDept showDept() {
        return baseDept;
    }

    @GetMapping("/showLine")
    public BaseLine showLine() {
        return baseLine;
    }

    @GetMapping("/showTerminal")
    public BaseTerminal showTerminal() {
        return baseTerminal;
    }

    @GetMapping("/showEmployee")
    public BaseEmployee showEmployee() {
        return baseEmployee;
    }

    @GetMapping("/showConfig")
    public UserConfig showConfig() {
        return userConfig;
    }

    @GetMapping("/showTeacher")
    public Teacher showTeacher() {
        return teacher;
    }

    @GetMapping("/showBook")
    public Book showBook() {
        System.out.println(book);
        return book;
    }
}
