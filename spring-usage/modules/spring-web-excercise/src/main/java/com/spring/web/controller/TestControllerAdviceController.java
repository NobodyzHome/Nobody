package com.spring.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.BatchUpdateException;

import javax.servlet.http.Part;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.validation.DataBinder;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

@Controller
@RequestMapping("/controllerAdvice")
public class TestControllerAdviceController {

	/*
	 * 注意：在调用@ModelAttribute、@ExceptionHandler、@InitBinder方法时，spring总是先调用@
	 * Controller类内部的这些注解对应的方法，然后再去调用@ControllerAdvice的方法。
	 * 
	 * 1.在调用@ExceptionHandler方法时，spring是优先调用@Controller类内部的对应方法来处理异常
	 * 例如：@Controller类中有NullPointerException类的@ExceptionHandler处理方法，
	 * 而ControllerAdvice中也有处理该异常的@ExceptionHandler处理方法，那么spring会调用@
	 * Controller类中的对应方法，而不是@ControllerAdvice中的对应方法来处理该异常。
	 * 
	 * 2.在调用@InitBinder、@ModelAttribute方法时，是先调用@Controller类内部的对应方法，然后调用所有（
	 * 注意是所有的）@ControllerAdvice类的对应方法。
	 * 这点和@ExceptionHandler不同，@ExceptionHandler是@Controller类有能处理该异常的方法，就不去@
	 * ControllerAdvice中寻找对应方法了。
	 */
	@ModelAttribute("zhangsan")
	public String zhangsan() {
		System.out.println("TestControllerAdviceController.zhangsan()");
		return "张三";
	}

	@InitBinder
	public void initBinder(DataBinder binder) {
		System.out.println("TestControllerAdviceController.initBinder()");
	}

	@RequestMapping("/checkModel")
	public View checkModel() {
		return new MappingJackson2JsonView();
	}

	@GetMapping(path = "/throw/{ex}")
	public void throwException(@PathVariable Class<? extends Exception> ex) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, InstantiationException, Exception {
		Exception exInstance = ConstructorUtils.invokeConstructor(ex);
		throw exInstance;
	}

	@PostMapping(path = { "/upload", "/up" }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public byte[] upload(@RequestPart byte[] file1, Part file2, @RequestParam MultipartFile file3) throws IOException {
		byte[] result = ArrayUtils.addAll(file1, StreamUtils.copyToByteArray(file2.getInputStream()));
		result = ArrayUtils.addAll(result, file3.getBytes());

		return result;
	}

	@ExceptionHandler
	public void handleBatchUpdateException(BatchUpdateException ex, PrintWriter out) {
		ex.printStackTrace(out);
	}

	@ExceptionHandler(IllegalStateException.class)
	@ResponseStatus(code = HttpStatus.BAD_GATEWAY, reason = "请求处理过程中发生错误")
	public void handleIllegalStateException() {
	}
}
