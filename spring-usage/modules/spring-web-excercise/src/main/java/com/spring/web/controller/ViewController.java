package com.spring.web.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;

import com.spring.web.view.ExcelView;

@Controller
@RequestMapping("/view")
public class ViewController {

	@ModelAttribute("hello")
	public String hello(String viewName) {
		return "hello:" + viewName;
	}

	@ModelAttribute("time")
	public Date date(@Value("2017-10-21") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
		return date;
	}

	@GetMapping(path = "/viewResolver/{condition}", params = "viewName")
	public String testViewResolver(String viewName, @PathVariable String condition, Model model) {
		return viewName;
	}

	@GetMapping(path = "/viewDirect", params = "viewClass")
	public View testView(@RequestParam("viewClass") Class<? extends View> clazz)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		View viewInstance = ConstructorUtils.invokeConstructor(clazz);
		return viewInstance;
	}

	@GetMapping("/excelView/{color}")
	public View testExcelView(@PathVariable String color, @Value("yoho=haha\nlisten=ting") Properties properties) {
		ExcelView view = new ExcelView();
		view.setAttributes(properties);
		view.setExposePathVariables(true);

		return view;
	}

}