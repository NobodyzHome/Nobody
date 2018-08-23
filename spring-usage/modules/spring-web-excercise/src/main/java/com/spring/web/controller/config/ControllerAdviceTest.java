package com.spring.web.controller.config;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;

import javax.sql.rowset.RowSetWarning;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.DataBinder;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ControllerAdviceTest {

	@ModelAttribute
	public String prefix(@Value("${urlPrefix}") String prefix) {
		System.out.println("ControllerAdviceTest.prefix()");
		return prefix;
	}

	@ExceptionHandler({ BatchUpdateException.class, RowSetWarning.class })
	public String handleSqlException(SQLException exception, Model model) {
		Date now = new Date();
		model.addAttribute("date", DateFormat.getDateInstance().format(now)).addAttribute("time",
				DateFormat.getTimeInstance().format(now));
		return "test";
	}

	@InitBinder
	public void init(DataBinder binder) {
		System.out.println("ControllerAdviceTest.init()");
	}

	@ExceptionHandler
	@ResponseStatus(code = HttpStatus.CONFLICT, reason = "媒体类型错误")
	public void handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
	}
}