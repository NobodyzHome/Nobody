package com.springboot.data.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.util.Date;

@Document(indexName = "index_user", type = "user", shards = 6, replicas = 1)
public class BaseUser implements Serializable {

    @Id
    @ReadOnlyProperty
    private String id;
    @Field(type = FieldType.Text, analyzer = "standard", fielddata = true)
    private String firstName;
    @Field(type = FieldType.Text, analyzer = "standard", fielddata = true)
    private String secondName;
    @Field(type = FieldType.Text, analyzer = "english", fielddata = true)
    private String code;
    @Field(type = FieldType.Keyword)
    private String password;
    @Field(type = FieldType.Boolean)
    private Boolean isValid;
    @Field(type = FieldType.Date)
    private Date createTime;
    @Field(type = FieldType.Date)
    private Date updateTime;
    @Field(type = FieldType.Integer)
    private Integer age;
    @Field(type = FieldType.Date)
    private Date birthDay;

    @ScriptedField
    @ReadOnlyProperty
    private Boolean isUpdated;
    @ScriptedField(name = "full")
    @ReadOnlyProperty
    private String fullName;
    @ScriptedField
    @ReadOnlyProperty
    private Boolean isOld;
    @Version
    @ReadOnlyProperty
    private Long version;
    @Score
    private Float score;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean valid) {
        isValid = valid;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Date getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public Boolean getUpdated() {
        return isUpdated;
    }

    public void setUpdated(Boolean updated) {
        isUpdated = updated;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getOld() {
        return isOld;
    }

    public void setOld(Boolean old) {
        isOld = old;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }
}
