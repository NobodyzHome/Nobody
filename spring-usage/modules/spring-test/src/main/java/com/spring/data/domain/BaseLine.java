package com.spring.data.domain;

import java.util.Date;

public class BaseLine {

	private Integer lineNo;
	private String lineCode;
	private String lineName;
	private Boolean isRun;
	private Date modifyDate;
	private BaseDept dept;

	public BaseLine(Integer lineNo, String lineCode) {
		super();
		this.lineNo = lineNo;
		this.lineCode = lineCode;
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

	public BaseDept getDept() {
		return dept;
	}

	public void setDept(BaseDept dept) {
		this.dept = dept;
	}

}
