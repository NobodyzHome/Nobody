package com.spring.data.domain;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

public class BaseTerminal {

	private Integer terminalNo;
	private String terminalCode;
	private BaseLine line;

	@JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", locale = "zh", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	private Date modifyDate;

	public Integer getTerminalNo() {
		return terminalNo;
	}

	public void setTerminalNo(Integer terminalNo) {
		this.terminalNo = terminalNo;
	}

	public String getTerminalCode() {
		return terminalCode;
	}

	public void setTerminalCode(String terminalCode) {
		this.terminalCode = terminalCode;
	}

	public BaseLine getLine() {
		return line;
	}

	public void setLine(BaseLine line) {
		this.line = line;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public BaseTerminal(Integer terminalNo) {
		super();
		this.terminalNo = terminalNo;
	}

	public BaseTerminal() {
		super();
	}

}
