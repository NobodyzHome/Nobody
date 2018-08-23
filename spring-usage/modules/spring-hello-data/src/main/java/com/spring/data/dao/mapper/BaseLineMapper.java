package com.spring.data.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.spring.data.domain.BaseLine;

public interface BaseLineMapper {
	public BaseLine[] selectByExample(BaseLine example);

	public List<BaseLine> selectById(@Param("lineNoArray") Integer... lineNos);

	public int insertByMap(@Param("map") Map<String, Object> paramMap);

	public int updateByExample(@Param("example") BaseLine example, @Param("conditions") Map<String, Object> conditions);

	public int deleteByConditions(@Param("conditions") Map<String, Object> conditions);

	public int deleteById(@Param("lineNoArray") Integer... lineNos);

	public void callFunction(Map<String, Object> paramMap);

	public void callProcedure(Map<String, Object> paramMap);

	public BaseLine testCache(@Param("lineNo") Integer lineNo);

	public BaseLine selectLineWithDept(@Param("lineNo") Integer lineNo);
}
