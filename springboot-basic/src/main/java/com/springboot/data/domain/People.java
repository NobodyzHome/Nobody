package com.springboot.data.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import java.util.Date;
import java.util.Map;

@ConfigurationProperties("config.people")
@Profile("people")
public class People {
    @NumberFormat(pattern = "No-#,###")
    private Integer age;
    private String name;
    private String remark;
    private String[] tags;
    private Map<String,Student> maps;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date modifyDate;

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public Map<String, Student> getMaps() {
        return maps;
    }

    public void setMaps(Map<String, Student> maps) {
        this.maps = maps;
    }
}
