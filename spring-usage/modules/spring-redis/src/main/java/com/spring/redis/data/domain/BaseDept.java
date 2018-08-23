package com.spring.redis.data.domain;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import org.springframework.data.annotation.Persistent;

public class BaseDept implements Serializable {

	private static final long serialVersionUID = 3714817180415858840L;

	@Id
	private Integer deptNo;
	private String deptCode;
	private String deptName;

	@Persistent
	@JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", locale = "zh", timezone = "GMT+8")
	private Date modifyDate;
	
	@JsonFormat(shape = Shape.NUMBER_INT)
	private Boolean isRun;

	public BaseDept(Integer deptNo, String deptCode, String deptName) {
		super();
		this.deptNo = deptNo;
		this.deptCode = deptCode;
		this.deptName = deptName;
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

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public void setIsRun(Boolean isRun) {
		this.isRun = isRun;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public Boolean getIsRun() {
		return isRun;
	}

}
