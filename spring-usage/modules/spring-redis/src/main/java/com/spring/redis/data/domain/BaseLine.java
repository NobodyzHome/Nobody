package com.spring.redis.data.domain;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;

public class BaseLine implements Serializable {

	private static final long serialVersionUID = -7842289025010703240L;
	
	@Id
	private Integer lineNo;
	private String lineCode;
	private String lineName;

	// @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", locale = "zh", timezone =
	// "GMT+8")
	private Date modifyDate;
	private Boolean isRun;
	private BaseDept dept;
	private BaseDept dept1;

	public BaseLine(Integer lineNo, String lineCode, String lineName) {
		super();
		this.lineNo = lineNo;
		this.lineCode = lineCode;
		this.lineName = lineName;
	}

	public BaseLine() {
		super();
	}

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

	public Boolean getIsRun() {
		return isRun;
	}

	public void setIsRun(Boolean isRun) {
		this.isRun = isRun;
	}

	public BaseDept getDept() {
		return dept;
	}

	public void setDept(BaseDept dept) {
		this.dept = dept;
	}

	public BaseDept getDept1() {
		return dept1;
	}

	public void setDept1(BaseDept dept1) {
		this.dept1 = dept1;
	}
}
