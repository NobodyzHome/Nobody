package com.spring.data.domain;

import java.util.Date;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

public class BaseDept {

	@NumberFormat(pattern = "No:#,###")
	@Min(value = 20, message = "{Min.deptNo}")
	private Integer deptNo;

	@Length(min = 2, max = 5)
	private String deptCode;

	@Pattern(regexp = "\\d+公司", message = "[message]公司名称的值具有不正确的格式")
	private String deptName;

	@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", locale = "zh", timezone = "GMT+8")
	@Past
	private Date modifyDate;

	@AssertTrue
	private Boolean isRun;

	public BaseDept(Integer deptNo, String deptCode) {
		super();
		this.deptNo = deptNo;
		this.deptCode = deptCode;
	}

	public BaseDept() {
		super();
	}

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

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public Boolean getIsRun() {
		return isRun;
	}

	public void setIsRun(Boolean isRun) {
		this.isRun = isRun;
	}

}
