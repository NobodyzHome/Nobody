package com.spring.data.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.spring.data.domain.BaseDept;

public interface BaseDeptMapper {
	public List<BaseDept> findByExample(BaseDept dept);

	public BaseDept findById(@Param("deptNo") Integer deptNo);

	public int insertByExample(BaseDept example);

	public int updateByParams(@Param("updateValues") Map<String, Object> updateValues,
			@Param("conditions") Map<String, Object> conditions);

	public int deleteByParams(@Param("deptNoArray") Integer[] deptNoArray,
			@Param("deptCodeArray") String[] deptCodeArray, @Param("isRun") Boolean isRun,
			@Param("parentDept") BaseDept parentDept);

	public void callFunction(Map<String, Object> paramMap);

	public void callProcedure(Map<String, Object> paramMap);

	public BaseDept selectDeptWithLines(@Param("deptNo") Integer deptNo);

	public BaseDept deptWithAllInfo(@Param("deptNo") Integer deptNo);
}
