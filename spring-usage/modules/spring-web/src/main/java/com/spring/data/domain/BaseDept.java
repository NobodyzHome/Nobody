package com.spring.data.domain;

import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

public class BaseDept {

	@NumberFormat(pattern = "#,###")
	@Max(9999)
	private Integer deptNo;

	@Length(min = 3, max = 10)
	@Pattern(regexp = "\\d+公司")
	@NotBlank
	private String deptName;

	@Past
	@NotNull
	@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", locale = "zh", timezone = "GMT+8")
	private Date modifyDate;
	
	private String deptCode;
	private Boolean isRun;
	private BaseDept parentDept;

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

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public Boolean getIsRun() {
		return isRun;
	}

	public void setIsRun(Boolean isRun) {
		this.isRun = isRun;
	}

	public BaseDept getParentDept() {
		return parentDept;
	}

	public void setParentDept(BaseDept parentDept) {
		this.parentDept = parentDept;
	}
}
