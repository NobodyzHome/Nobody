package com.spring.data.domain;

import java.util.Date;

public class BaseRole {

	private Integer roleNo;
	private String roleName;
	private String description;
	private Boolean active;
	private Date modifyDate;
	private Integer roleGroupNo;
	private Integer lineGroupNo;

	public BaseRole(Integer roleNo, String roleName) {
		super();
		this.roleNo = roleNo;
		this.roleName = roleName;
	}

	public BaseRole() {
		super();
	}

	public Integer getRoleNo() {
		return roleNo;
	}

	public void setRoleNo(Integer roleNo) {
		this.roleNo = roleNo;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String desciption) {
		this.description = desciption;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public Integer getRoleGroupNo() {
		return roleGroupNo;
	}

	public void setRoleGroupNo(Integer roleGroupNo) {
		this.roleGroupNo = roleGroupNo;
	}

	public Integer getLineGroupNo() {
		return lineGroupNo;
	}

	public void setLineGroupNo(Integer lineGroupNo) {
		this.lineGroupNo = lineGroupNo;
	}

}
