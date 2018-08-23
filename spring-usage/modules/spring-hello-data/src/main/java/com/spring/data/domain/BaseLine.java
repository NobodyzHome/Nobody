package com.spring.data.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

public class BaseLine implements Serializable {

	private static final long serialVersionUID = 8265661048320103649L;
	private Integer lineNo;
	private String lineCode;
	private String lineName;

	@JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", locale = "zh", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	private Date modifyDate;
	private Boolean isRun;
	private BaseDept dept;
	private List<BaseTerminal> terminals;

	public BaseLine(Integer lineNo, String lineCode, String lineName) {
		super();
		this.lineNo = lineNo;
		this.lineCode = lineCode;
		this.lineName = lineName;
	}

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

	public List<BaseTerminal> getTerminals() {
		return terminals;
	}

	public void setTerminals(List<BaseTerminal> terminals) {
		this.terminals = terminals;
	}

}
