package com.spring.data.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

public class BaseDept implements Serializable {

	private static final long serialVersionUID = -9219506725658499140L;

	@NumberFormat(pattern = "第#号")
	@Max(1000)
	private Integer deptNo;

	@Length(min = 1, max = 5)
	private String deptCode;

	@Pattern(regexp = "\\d{2,}公司")
	private String deptName;

	@AssertTrue
	private Boolean isRun;

	@Past
	@DateTimeFormat(pattern = "yyyy/MM/dd kk:mm:ss")
	private Date modifyDate;

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

	@Override
	public boolean equals(Object obj) {
		boolean result;
		if (Objects.nonNull(obj)) {
			if (obj == this) {
				result = true;
			} else if (obj instanceof BaseDept) {
				BaseDept another = (BaseDept) obj;
				result = getDeptNo().equals(another.getDeptNo()) && getDeptCode().equals(another.getDeptCode())
						&& getDeptName().equals(another.getDeptName());
			} else {
				result = false;
			}
		} else {
			result = false;
		}

		return result;
	}

	@Override
	public int hashCode() {
		return getDeptNo();
	}
}
