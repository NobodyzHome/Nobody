package com.spring.data.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.util.Date;


/**
 * @author maziqiang
 * @Document注解用于设置当前这个类在ES中对应的索引，例如索引名是什么，映射名是什么，索引的主分片数，索引的备份分片数
 */
@Document(indexName = "index_line", type = "line", shards = 6, replicas = 2)
public class BaseLine implements Serializable {


    /**
     * Id注解标识出该字段用于代表文档的ID：
     * 1.在存储BaseLine对象时，如果这个属性没有值，那么就是由ES来生成这个文档的ID，相当于POST方式来索引文档
     * 2.在存储BaseLine对象时，如果这个属性有值，那么就是使用这个值作为文档的ID，相当于PUT方式来索引文档
     * 3.在查询时，会把文档的ID的值赋值给这个属性
     * <p>
     * 注意：
     * Id注解所在的属性必须和库中id字段的类型一致，这样才能将库中id字段的值赋值到@Id注解所对应的属性中。（例如base_dept表中的dept_no字段是int，那么BaseDept类的id属性是Integer类型的）
     * 由于在ES中，一个文档的ID必然是字符串，因此@Id注解所注解的属性的类型也必须为String。
     */
    @Id
    @ReadOnlyProperty
    private String id;
    @Field(type = FieldType.Integer)
    private Integer lineNo;
    /**
     * 使用@Field注解来说明ES中该属性的类型
     * type = FieldType.Text说明该属性是全文域，即会被分析
     * analyzer = "simple"说明该属性是被简单分析器来分析
     * fielddata = true说明这个属性的fielddata为true。在ES中，只有fielddata设置为true的全文域属性或精确值域属性才可以用来进行聚合操作
     * index = true说明这个属性需要被索引到ES的文档中，一个属性必须要被索引到文档中，才可以使用该属性进行文档的搜索
     */
    @Field(type = FieldType.Text, analyzer = "simple", fielddata = true, index = true)
    private String lineCode;
    @Field(type = FieldType.Text, analyzer = "standard", fielddata = true)
    private String lineName;
    @Field(type = FieldType.Text, analyzer = "english", fielddata = true)
    private String lineEnName;
    @Field(type = FieldType.Keyword)
    private String memo;
    @Field(type = FieldType.Date)
    private Date createTime;
    @Field(type = FieldType.Date)
    private Date updateTime;
    /**
     * @ScriptedField是spring-data-es的注解，spring-data-es在查询时，会把es中返回的文档的script_fields里名为isUpdated的field赋值到该属性中
     * @ReadOnlyProperty是spring-data的注解，说明当前属性只是通过查询获取的值，不会保存到数据库中。这样使用spring-data把这个对象存储到ES中时，不会存储该字段
     */
    @ReadOnlyProperty
    @ScriptedField
    private Boolean isUpdated;
    @ScriptedField(name = "multiLineNo")
    @ReadOnlyProperty
    private Long multipleLineNo;
    /**
     * @Version是spring-data的注解，spring-data-es在查询时，会把es中返回的文档的version值存储到@Version对应的属性中
     */
    @Version
    @ReadOnlyProperty
    private Long version;
    /**
     * Score注解是spring-data-es的注解，spring-data-es在查询时，会把es中返回的文档的score值存储到@Scrore对应的属性中
     * 注意：scroe属性必须是float或Float类型的 -- Score property score must be either of type float or Float!
     * 由于@Score注解被@ReadOnlyProperty注解了，因此在当前属性上不用增加@ReadOnlyProperty注解
     */
    @Score
    private Float score;

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

    public String getLineEnName() {
        return lineEnName;
    }

    public void setLineEnName(String lineEnName) {
        this.lineEnName = lineEnName;
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

    public Boolean getUpdated() {
        return isUpdated;
    }

    public void setUpdated(Boolean updated) {
        isUpdated = updated;
    }

    public Long getMultipleLineNo() {
        return multipleLineNo;
    }

    public void setMultipleLineNo(Long multipleLineNo) {
        this.multipleLineNo = multipleLineNo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
