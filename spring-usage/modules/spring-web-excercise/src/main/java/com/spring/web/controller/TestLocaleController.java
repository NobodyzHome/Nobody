package com.spring.web.controller;

import java.util.Locale;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.RequestContext;

@Controller
@ResponseStatus(code = HttpStatus.ALREADY_REPORTED)
public class TestLocaleController {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private Environment environment;

	@GetMapping("/locale/i18n/{code}")
	@ResponseBody
	public String i18nTranslate(@PathVariable String code, @RequestParam(required = false) Object[] args,
			Locale locale) {
		String message = messageSource.getMessage(code, args, code, locale);
		return message;
	}

	@GetMapping(path = { "/nonLocale", "/showLocale" })
	@ResponseBody
	public String nonLocale(Locale locale) {
		return locale.toString();
	}

	@GetMapping("/locale/change/{locale}")
	@ResponseStatus(code = HttpStatus.FOUND)
	public String changeLocale(@PathVariable Locale locale, Boolean isForward, HttpServletRequest request,
			HttpServletResponse response) {
		RequestContext context = new RequestContext(request, response);
		context.changeLocale(locale);

		isForward = Objects.isNull(isForward) ? true : isForward;
		String location = (isForward ? "forward" : "redirect") + ":${urlPrefix}/showLocale";
		return environment.resolveRequiredPlaceholders(location);
	}

}