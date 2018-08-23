package com.springboot.data.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@ConfigurationProperties("teacher")
//@Validated
public class Teacher {
    @NumberFormat(pattern = "No-#,###")
    @Range(min = 100, max = 2000)
    private Integer id;

    @Length(min = 2, max = 10)
    private String name;
    @NotBlank
    private String sex;

    @Past
    @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", locale = "zh", timezone = "GMT+8")
    private Date birthDay;

    // 对于内嵌的复杂类型的属性，尽管不加@Valid注解，spring boot也会对其验证，但一个好的习惯是对内嵌的复杂类型的属性加上@Valid注解
    // Although nested properties will also be validated when bound, it’s good practice to also annotate the associated field as @Valid
    // This ensure that validation is triggered even if no nested properties are found.
    @Valid
    private List<Student> students;

    @Size(min = 5, max = 10)
    private String[] comments;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Date getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public String[] getComments() {
        return comments;
    }

    public void setComments(String[] comments) {
        this.comments = comments;
    }
}
