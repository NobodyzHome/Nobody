package com.spring.data.domain;

import java.util.Date;

public class BaseRoleLine {

	private BaseRole role;
	private BaseLine line;
	private Boolean operate;
	private Date modifyDate;

	public BaseRole getRole() {
		return role;
	}

	public void setRole(BaseRole role) {
		this.role = role;
	}

	public BaseLine getLine() {
		return line;
	}

	public void setLine(BaseLine line) {
		this.line = line;
	}

	public Boolean getOperate() {
		return operate;
	}

	public void setOperate(Boolean operate) {
		this.operate = operate;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

}
