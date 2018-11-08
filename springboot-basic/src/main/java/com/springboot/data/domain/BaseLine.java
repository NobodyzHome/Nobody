package com.springboot.data.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import java.util.Date;

@ConfigurationProperties("line")
@Document(indexName = "index_line", type = "line", shards = 6, replicas = 1)
public class BaseLine {

    @Id
    @ReadOnlyProperty
    private String id;

    @Field(type = FieldType.Integer)
    @NumberFormat(pattern = "No:#,###")
    private Integer lineNo;
    @Field(type = FieldType.Text, analyzer = "english")
    private String lineCode;
    @Field(type = FieldType.Keyword)
    private String lineName;

    @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", locale = "zh", timezone = "GMT+8")
    @Field(type = FieldType.Date)
    private Date modifyDate;

    @ReadOnlyProperty
    private String[] notes;

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }

    public String getLineCode() {
        return lineCode;
    }

    public void setLineCode(String lineCode) {
        this.lineCode = lineCode;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String[] getNotes() {
        return notes;
    }

    public void setNotes(String[] notes) {
        this.notes = notes;
    }
}
