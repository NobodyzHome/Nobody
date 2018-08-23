package com.spring.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/theme")
public class TestThemeController {

	@ModelAttribute("prefix")
	public String dispatcherPath(@Value("${urlPrefix}") String dispatcherPath) {
		return dispatcherPath;
	}

	@RequestMapping(path = "/excercise", params = "view")
	public String testTheme(String view) {
		return view;
	}
}