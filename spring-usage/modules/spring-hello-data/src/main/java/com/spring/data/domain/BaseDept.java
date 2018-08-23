package com.spring.data.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

public class BaseDept implements Serializable {

	private static final long serialVersionUID = 8397357604177466052L;

	@NumberFormat(pattern = "No:#,###")
	@NotNull
	@Range(min = 50, max = 2000)
	private Integer deptNo;

	@NotBlank
	@Length(min = 2, max = 4)
	private String deptCode;

	@NotBlank
	@Length(min = 4, max = 6)
	@Pattern(regexp = "\\d{2,4}公司", message = "【message】公司名称不符合格式要求")
	private String deptName;

	@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", locale = "zh", timezone = "GMT+8")
	@NotNull
	@Past(message = "{Past.message}")
	private Date modifyDate;

	@AssertTrue
	private Boolean isRun;
	private Integer deptLevel;
	private BaseDept parent;
	private List<BaseLine> lines;
	private Set<BaseLine> lineSet;

	public BaseDept(Integer deptNo, String deptCode) {
		super();
		this.deptNo = deptNo;
		this.deptCode = deptCode;
	}

	public BaseDept() {
		this(null, null);
	}

	public Integer getDeptNo() {
		return deptNo;
	}

	public String getDeptCode() {
		return deptCode;
	}

	public String getDeptName() {
		return deptName;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public Boolean getIsRun() {
		return isRun;
	}

	public void setDeptNo(Integer deptNo) {
		this.deptNo = deptNo;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public void setIsRun(Boolean isRun) {
		this.isRun = isRun;
	}

	public Integer getDeptLevel() {
		return deptLevel;
	}

	public void setDeptLevel(Integer deptLevel) {
		this.deptLevel = deptLevel;
	}

	public BaseDept getParent() {
		return parent;
	}

	public void setParent(BaseDept parent) {
		this.parent = parent;
	}

	public List<BaseLine> getLines() {
		return lines;
	}

	public void setLines(List<BaseLine> lines) {
		this.lines = lines;
	}

	public Set<BaseLine> getLineSet() {
		return lineSet;
	}

	public void setLineSet(Set<BaseLine> lineSet) {
		this.lineSet = lineSet;
	}

}
