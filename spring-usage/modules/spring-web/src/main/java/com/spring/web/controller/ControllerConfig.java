package com.spring.web.controller;

import java.util.Locale;

import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.spring.web.converter.BaseDeptFormatter;

// @ControllerAdvice主要用于针对多个Controller，配置统一的@InitBinder,@ModelAttribute和@ExceptionHandler
@ControllerAdvice(annotations = Controller.class)
public class ControllerConfig {

	// 注意：在@ModelAttribute方法中，也可以使用@RequestParam,@PathVariable等注解
	@ModelAttribute("hello")
	public String hello(String words) {
		return words;
	}

	@ResponseBody
	@ExceptionHandler
	public String handlerException(Exception exception,
			@SessionAttribute(required = false, name = "locale") Locale locale) {
		return locale + "发生异常：" + exception.getMessage();
	}

	@InitBinder
	public void initBinder(WebDataBinder dataBinder) {
		BaseDeptFormatter deptFormatter = new BaseDeptFormatter();
		ConversionService conversionService = dataBinder.getConversionService();
		if (FormattingConversionService.class.isInstance(conversionService)) {
			FormattingConversionService formattingConversionService = FormattingConversionService.class
					.cast(conversionService);
			formattingConversionService.addFormatter(deptFormatter);
		}
	}
}