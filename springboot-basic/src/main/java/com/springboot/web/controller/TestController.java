package com.springboot.web.controller;

import com.springboot.data.domain.Line;
import com.springboot.data.domain.People;
import com.springboot.data.domain.Home;
import com.springboot.data.domain.Student;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    public Student student;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private People people;

    @Autowired
    private HomeRepository homeRepository;

    @Autowired
    private LineRepository lineRepository;

    @GetMapping("/view/{viewName}")
    public String view(@PathVariable String viewName, Model model) {
        model.addAttribute("view", viewName).addAttribute("wawa", "还真不错哇").addAttribute(student)
                .addAttribute(new Date()).addAttribute("helloWorld", "asdfdsf");
        List<Student> studentList = studentRepository.findAll();
        model.addAttribute("students", studentList);
        model.addAttribute("user", "zhangsan");
        model.addAttribute("htmlContents", "<b>Welcome</b>");

        return "test";
    }

    @GetMapping("/excercise/{hello}/{world}")
    public String excercise(@PathVariable String hello, @PathVariable String world, Model model) {
        model.addAttribute("hello", hello).addAttribute(people).addAttribute("world", world);

        List<Student> students = mongoTemplate.findAll(Student.class);
        model.addAttribute(students).addAttribute(student);

        Set<Student> maleStudents = studentRepository.findBySexIgnoreCase("男");
        model.addAttribute("maleStudents", maleStudents);
        model.addAttribute("date", new Date());
        model.addAttribute(student);

        return "excercise/show";
    }

    @GetMapping("/queryNear")
    @ResponseBody
    public List<Home> queryNear(double x, double y, double min, double max) {
        List<Home> byPosNear = homeRepository.findByPosNear(x, y, min, max);
        ObjectId id = byPosNear.get(0).getId();
        Home byId = homeRepository.findById(id);
        System.out.println(byId);

        Home home = new Home();
        home.setName("test");
        Home save = homeRepository.save(home);

        return byPosNear;
    }

    @GetMapping("/test")
    @ResponseBody
    public List<Line> test() {
        List<Line> lines = lineRepository.findLines();
        Line line = lines.get(0);

        List<Line> byIdTest= lineRepository.findByIdTest(line.getId());
        return byIdTest;
    }
}
