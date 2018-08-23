package com.spring.data.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.spring.data.domain.BaseRole;

public interface BaseRoleMapper {
	public List<BaseRole> queryRoles(@Param("roleNoArray") int[] roleNoArray, @Param("roleNo") Integer roleNo);
}
