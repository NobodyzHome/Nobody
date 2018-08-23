package com.springboot.data.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ConfigurationProperties("bk")
@Profile({"book", "bk"})
@Validated
public class Book {

    @NotNull
    @Min(1000)
    private Integer id;

    @NotBlank
    private String name;

    @Size(min = 2, max = 10)
    private List<String> alias;

    @Length(min = 2, max = 4)
    private String authorName;

    @Size(max = 5)
    private List<String> corAuthor;

    @NumberFormat(style = NumberFormat.Style.CURRENCY)
    private Double sellPrice;

    @Future
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", locale = "zh", timezone = "GMT+8")
    private Date publishDate;

    @Max(20000)
    @NumberFormat(pattern = "pub-num-#,###")
    private Long publishNum;

    @NotNull
    @Valid
    private Category category;

    private Map<String, Double> specialPrice;
    private Map<String, Category> specialCategory;

    static class Category {
        @NotBlank
        private String name;
        @NotBlank
        private String code;

        @Length(min = 2, max = 7)
        private String searchCode;

        @Past
        @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
        @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", locale = "zh", timezone = "GMT+8")
        private Date modifyDate;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getSearchCode() {
            return searchCode;
        }

        public void setSearchCode(String searchCode) {
            this.searchCode = searchCode;
        }

        public Date getModifyDate() {
            return modifyDate;
        }

        public void setModifyDate(Date modifyDate) {
            this.modifyDate = modifyDate;
        }
    }

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

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(Double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public Long getPublishNum() {
        return publishNum;
    }

    public void setPublishNum(Long publishNum) {
        this.publishNum = publishNum;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<String> getAlias() {
        return alias;
    }

    public void setAlias(List<String> alias) {
        this.alias = alias;
    }

    public Map<String, Double> getSpecialPrice() {
        return specialPrice;
    }

    public void setSpecialPrice(Map<String, Double> specialPrice) {
        this.specialPrice = specialPrice;
    }

    public Map<String, Category> getSpecialCategory() {
        return specialCategory;
    }

    public void setSpecialCategory(Map<String, Category> specialCategory) {
        this.specialCategory = specialCategory;
    }

    public List<String> getCorAuthor() {
        return corAuthor;
    }

    public void setCorAuthor(List<String> corAuthor) {
        this.corAuthor = corAuthor;
    }
}
