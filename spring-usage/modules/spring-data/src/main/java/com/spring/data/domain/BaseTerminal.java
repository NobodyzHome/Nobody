package com.spring.data.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.util.Date;

@Document(indexName = "index_terminal", type = "terminal", shards = 8, replicas = 2)
public class BaseTerminal implements Serializable {

    @Id
    @ReadOnlyProperty
    private String terminalNo;

    @Field(type = FieldType.Text, analyzer = "english")
    private String terminalCode;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String terminalName;

    @Field(type = FieldType.Keyword)
    private String plateCode;

    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Date)
    private Date updateTime;

    @Field(type = FieldType.Keyword)
    private String memo;

    @Field(type = FieldType.Integer)
    private Integer lineNo;

    @Field(type = FieldType.Text, analyzer = "simple")
    private String remark;

    @Score
    private Float score;

    @Version
    @ReadOnlyProperty
    private Long version;

    @ScriptedField
    @ReadOnlyProperty
    private Boolean isUpdated;

    @ScriptedField(name = "mLineNo")
    @ReadOnlyProperty
    private Integer multipleLineNo;

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getTerminalCode() {
        return terminalCode;
    }

    public void setTerminalCode(String terminalCode) {
        this.terminalCode = terminalCode;
    }

    public String getTerminalName() {
        return terminalName;
    }

    public void setTerminalName(String terminalName) {
        this.terminalName = terminalName;
    }

    public String getPlateCode() {
        return plateCode;
    }

    public void setPlateCode(String plateCode) {
        this.plateCode = plateCode;
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

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Boolean getUpdated() {
        return isUpdated;
    }

    public void setUpdated(Boolean updated) {
        isUpdated = updated;
    }

    public Integer getMultipleLineNo() {
        return multipleLineNo;
    }

    public void setMultipleLineNo(Integer multipleLineNo) {
        this.multipleLineNo = multipleLineNo;
    }
}
