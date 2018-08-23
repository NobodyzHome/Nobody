package com.spring.web.controller;

import com.spring.data.domain.Student;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.nio.charset.Charset;

@Controller
@RequestMapping("/basic")
public class BasicController {

    @GetMapping("/viewName/{view}")
    public String viewName(@PathVariable String view) {
        return view;
    }

    @PostMapping("/direct")
    public void direct(@RequestBody Student student, PrintWriter out) {
        out.print(student.getName());
    }

    @RequestMapping("/responseBody/{id}/{name}/{sex}/{birthDay}")
    @ResponseBody
    public Student responseBody(Student student, @Value("#{charset}") Charset charset) {
        student.setName(student.getName() + charset.name());
        return student;
    }

    @RequestMapping("/test/{words}")
    @ResponseBody
    public String test(@PathVariable String words) {
        return words;
    }
}
