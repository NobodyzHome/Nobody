package com.spring.data.dao;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import org.apache.ibatis.annotations.Param;
import org.hibernate.validator.constraints.Range;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;

import com.spring.data.domain.BaseRoleLine;

@Validated
@CacheConfig(cacheNames = "hello")
public interface BaseRoleLineMapper {

	@Size(min = 1, max = 10)
	@NotNull
	@Cacheable(condition = "args[0]<10", unless = "#result.size()<=0")
	public List<BaseRoleLine> findByLine(@Min(value = 0, message = "{Min}") @Param("lineNo") Integer lineNo,
			@Param("operate") Boolean operate);

	@Size(min = 0, max = 5)
	public List<BaseRoleLine> findByRole(@Min(value = 0, message = "{Min}") @Param("roleNo") Integer roleNo,
			@AssertTrue @Param("operate") Boolean operate);

	@NotNull
	public BaseRoleLine fineByRoleLine(@Range(min = 1, max = 200, message = "{Range}") @Param("roleNo") Integer roleNo,
			@Max(10) @Param("lineNo") Integer lineNo, @Param("operate") Boolean operate);

	@Size(min = 1, max = 100)
	public List<BaseRoleLine> findByOperate(@Param("operate") Boolean operate);

	@Size(min = 10, max = 80)
	public List<BaseRoleLine> findByModifyDate(@Past @Param("modifyDate") Date modifyDate);
}
