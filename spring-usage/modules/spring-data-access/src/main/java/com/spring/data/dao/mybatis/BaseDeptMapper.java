package com.spring.data.dao.mybatis;

import org.apache.ibatis.annotations.Param;

import com.spring.data.domain.BaseDept;

public interface BaseDeptMapper {
	public BaseDept[] queryDept(BaseDept condition);

	public int insertDept(BaseDept deptToInsert);

	public int queryDeptId();

	public int updateDept(@Param("dept") BaseDept deptToUpdate, @Param("condition") BaseDept condition);

	public int deleteDept(BaseDept dept);
}
