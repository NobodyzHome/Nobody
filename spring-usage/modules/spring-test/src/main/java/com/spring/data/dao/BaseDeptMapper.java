package com.spring.data.dao;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.ibatis.annotations.Param;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;

import com.spring.data.domain.BaseDept;

@Validated
@CacheConfig(cacheNames = "dept")
public interface BaseDeptMapper {

	public List<BaseDept> queryDepts(@Param("deptName") String deptName);

	@NotNull
	@Cacheable(condition = "args[0]<=8888")
	public BaseDept queryDept(@Min(value = 0, message = "{Min}") @Param("deptNo") Integer deptNo);

	public List<BaseDept> queryDeptAndLine(@Param("deptNo") Integer deptNo);

	@Cacheable
	public BaseDept queryDeptSelectLines(@Param("deptNo") Integer deptNo);
}
