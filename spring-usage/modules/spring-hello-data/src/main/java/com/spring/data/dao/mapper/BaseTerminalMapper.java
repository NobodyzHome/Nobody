package com.spring.data.dao.mapper;

import org.apache.ibatis.annotations.Param;

import com.spring.data.domain.BaseTerminal;

public interface BaseTerminalMapper {

	public BaseTerminal findById(@Param("terminalNo") Integer terminalNo);

	public BaseTerminal findByIdWithLines(@Param("terminalNo") Integer terminalNo);
}
