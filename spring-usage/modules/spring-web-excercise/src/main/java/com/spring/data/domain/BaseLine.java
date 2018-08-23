package com.spring.data.domain;

import java.util.Date;

import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spring.data.validation.group.Insert;
import com.spring.data.validation.group.Update;

@GroupSequence({ Insert.class, Update.class, BaseLine.class })
public class BaseLine {

	// 将该验证条件放到Insert.class这个分组下
	@Min(value = 30, groups = Insert.class)
	// 将该验证条件放到Update.class这个分组下
	@Max(value = 1000, groups = Update.class)
	private Integer lineNo;

	// 没有设置groups属性，那么将该验证条件放到Default.class这个分组下
	@Length(min = 3, max = 10)
	@NotBlank(groups = Insert.class)
	private String lineCode;

	// 将该验证条件同时放到Insert.class, Update.class这两个分组下
	@Pattern(regexp = "\\d+线路", groups = { Insert.class, Update.class })
	@Length(min = 3, max = 5)
	private String lineName;

	@JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", locale = "zh", timezone = "GMT+8")
	@NotNull
	@Past(groups = Insert.class)
	private Date modifyDate;

	@AssertTrue(groups = { Insert.class, Update.class })
	private Boolean isRun;

	@NotNull
	private BaseDept dept;

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
}
