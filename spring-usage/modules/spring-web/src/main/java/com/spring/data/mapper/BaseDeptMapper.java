package com.spring.data.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.spring.data.domain.BaseDept;

public interface BaseDeptMapper {

	public List<BaseDept> queryDept(BaseDept condition);

	public void timeCha(Map<String, Object> paramMap);

	public Integer timeCha1(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
