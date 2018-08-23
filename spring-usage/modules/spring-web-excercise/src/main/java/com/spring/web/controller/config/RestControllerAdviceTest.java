package com.spring.web.controller.config;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// annotations = RestController.class说明当前ControllerAdivce中的内容只对被@RestController注解的类起作用
@RestControllerAdvice(annotations = { RestController.class, Controller.class })
public class RestControllerAdviceTest {

	@ModelAttribute("lisi")
	public String lisi() {
		System.out.println("RestControllerAdviceTest.lisi()");
		return "李四";
	}

	// 在@RestControllerAdvice下，相当于又加了一个@ResponseBody注解
	@ExceptionHandler
	@ResponseStatus(code = HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
	public NullPointerException handleNullPointerException(NullPointerException ex) {
		return ex;
	}

	@InitBinder
	public void binder(DataBinder binder) {
		System.out.println("RestControllerAdviceTest.binder()");
	}
}
