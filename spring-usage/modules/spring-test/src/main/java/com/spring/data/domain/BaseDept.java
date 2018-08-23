package com.spring.data.domain;

import java.util.Date;
import java.util.Set;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

public class BaseDept {

	@Range(min = 3, max = 10, message = "{Range}")
	@Positive
	private Integer deptNo;

	@Pattern(regexp = "\\d{3,}")
	@Length(min = 1, max = 8)
	private String deptCode;

	@Length(min = 2, max = 10)
	private String deptName;

	@AssertTrue
	private Boolean isRun;

	@Past
	private Date modifyDate;

	private Set<BaseLine> lines;

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

	public Boolean getIsRun() {
		return isRun;
	}

	public void setIsRun(Boolean isRun) {
		this.isRun = isRun;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public Set<BaseLine> getLines() {
		return lines;
	}

	public void setLines(Set<BaseLine> lines) {
		this.lines = lines;
	}
}
