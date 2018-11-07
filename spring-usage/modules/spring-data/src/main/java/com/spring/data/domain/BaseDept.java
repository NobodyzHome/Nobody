package com.spring.data.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.ScriptedField;

import java.util.Date;

@Document(indexName = "index_dept", type = "dept", replicas = 2, shards = 6)
public class BaseDept {

    @Id
    @Field(type = FieldType.Integer)
    private Integer deptNo;
    /*
        1.在聚合时，如果要对全文域的字段（type=text）进行聚合，必须在映射中设置该字段的fielddata属性为true，否则会报以下错误：
        Set fielddata=true on [deptName] in order to load fielddata in memory by uninverting the inverted index. Note that this can however use significant memory. Alternatively use a keyword field instead.

        2.但是在聚合时，如果要对精确值域的字段（type=keyword）进行聚合，则不需要设置该字段的fielddata属性
    */
    @Field(type = FieldType.Text, index = true, analyzer = "standard", fielddata = true)
    private String deptCode;
    @Field(type = FieldType.Text, index = true, analyzer = "standard")
    private String deptName;
    @Field(type = FieldType.Text, index = true, analyzer = "english")
    private String deptEnName;
    @Field(type = FieldType.Date)
    private Date modifyDate;
    @Field(type = FieldType.Integer)
    private Integer level;
    @Field(type = FieldType.Keyword, index = false)
    private String remark;
    @Field(type = FieldType.Date)
    private Date createTime;
    // 设置将查询结果中的script_fields里哪个名字的字段赋值到当前这个属性中
    @ScriptedField
    private String prefixRemark;

    public Integer getDeptNo() {
        return deptNo;
    }

    public void setDeptNo(Integer deptNo) {
        this.deptNo = deptNo;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptEnName() {
        return deptEnName;
    }

    public void setDeptEnName(String deptEnName) {
        this.deptEnName = deptEnName;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getPrefixRemark() {
        return prefixRemark;
    }

    public void setPrefixRemark(String prefixRemark) {
        this.prefixRemark = prefixRemark;
    }
}
