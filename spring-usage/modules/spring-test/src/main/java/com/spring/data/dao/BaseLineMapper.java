package com.spring.data.dao;

import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.hibernate.validator.constraints.Range;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.validation.annotation.Validated;

import com.spring.data.domain.BaseLine;

@Validated
@CacheConfig(cacheNames = "line")
public interface BaseLineMapper {

	public BaseLine[] queryLines(@Param("deptNo") Integer deptNo, @Param("lineNoArray") Integer[] lineNoArray,
			@Param("isRun") Boolean lineIsRun);

	public BaseLine[] queryLinesWithPrefix(@Param("deptNo") Integer deptNo, @Param("lineNoArray") Integer[] lineNoArray,
			@Param("isRun") Boolean lineIsRun);

	public BaseLine[] queryLineBasic(@Param("lineNo") Integer lineNo, @Param("lineName") String lineName);

	@Caching(cacheable = @Cacheable(condition = "args[0]>2222", unless = "T(java.util.Objects).isNull(#result)"), evict = @CacheEvict(beforeInvocation = true, allEntries = true, condition = "args[0]<0"))
	public BaseLine queryLineSelectDept(@Param("lineNo") @Range(min = -1000, max = 10000) Integer lineNo);

	public void callFunction(Map<String, Object> paramMap);

	public BaseLine[] queryLineByDept(@Param("deptNo") Integer deptNo);
}
