package com.spring.data.domain;

public class BaseLineTeam {

	private Integer groupNo;
	private String groupName;
	private String description;
	private BaseDept dept;

	public Integer getGroupNo() {
		return groupNo;
	}

	public void setGroupNo(Integer groupNo) {
		this.groupNo = groupNo;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BaseDept getDept() {
		return dept;
	}

	public void setDept(BaseDept dept) {
		this.dept = dept;
	}

}
