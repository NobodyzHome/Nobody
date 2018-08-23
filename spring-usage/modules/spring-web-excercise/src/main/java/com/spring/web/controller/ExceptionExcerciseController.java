package com.spring.web.controller;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;
import javax.sql.rowset.RowSetWarning;
import javax.sql.rowset.serial.SerialException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.spring.data.domain.BaseDept;

@Controller
@RequestMapping("/exception/excercise")
public class ExceptionExcerciseController {

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private Environment environment;

	@RequestMapping("/throw/{ex}")
	public void throwException(@PathVariable("ex") Class<? extends Exception> exceptionClass,
			@MatrixVariable(pathVar = "ex", required = false) String param) throws Exception {
		Object[] params = StringUtils.hasText(param) ? ArrayUtils.toArray(param) : null;
		Exception ex = ConstructorUtils.invokeConstructor(exceptionClass, params);
		throw ex;
	}

	@ExceptionHandler
	@ResponseStatus(code = HttpStatus.BAD_GATEWAY)
	public String handleSqlException(SQLException exception, Model model, @SessionAttribute BaseDept dept) {
		model.addAttribute(exception).addAttribute("dept", dept);
		return "err";
	}

	@ExceptionHandler({ RowSetWarning.class, SerialException.class })
	@ResponseStatus(code = HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
	public void handleRowSetWarning(PrintWriter out, SQLException ex) {
		out.println("请求处理发生异常");
		ex.printStackTrace(out);
	}

	@ExceptionHandler(NullPointerException.class)
	@ResponseBody
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public Resource handleNullPointerException(HttpServletResponse response) {
		Resource resource = resourceLoader.getResource("classpath:spring/container/springMvc-servlet.xml");
		response.addHeader("Content-Disposition", "attachement;fileName=" + resource.getFilename());
		return resource;
	}

	@ExceptionHandler
	public View handleIllegalStateException(IllegalStateException exception, Model model,
			@SessionAttribute BaseDept dept) {
		model.addAttribute(new Date()).addAttribute("dept", dept.getDeptName()).addAttribute("info",
				environment.resolvePlaceholders("你好,${user.name}，contextId是${contextId}"));
		View view = new MappingJackson2JsonView();
		return view;
	}

	@ExceptionHandler(ArrayIndexOutOfBoundsException.class)
	@ResponseStatus(code = HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, reason = "在请求处理过程中发生了运行错误！")
	public void hanleArrayIndexOutOfBoundsException() {
	}

	@ResponseStatus(code = HttpStatus.EXPECTATION_FAILED, reason = "exception.test1")
	public static class TestException1 extends Exception {
	}

	@ResponseStatus(code = HttpStatus.GATEWAY_TIMEOUT, reason = "发生了TestException2异常")
	public static class TestException2 extends Exception {
	}
}
