package com.springboot.web.controller;

import com.springboot.data.domain.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    public Student student;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/view/{viewName}")
    public String view(@PathVariable String viewName, Model model) {
        model.addAttribute("view", viewName).addAttribute("wawa", "还真不错哇").addAttribute(student)
                .addAttribute(new Date()).addAttribute("helloWorld", "asdfdsf");
        List<Student> studentList = studentRepository.findAll();
        model.addAttribute("students", studentList);
        model.addAttribute("user", "zhangsan");
        model.addAttribute("htmlContents","<b>Welcome</b>");

        return viewName;
    }
}
