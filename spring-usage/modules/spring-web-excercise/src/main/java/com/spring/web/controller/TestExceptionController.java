package com.spring.web.controller;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;

import javax.security.cert.CertificateException;
import javax.security.cert.CertificateNotYetValidException;
import javax.security.cert.CertificateParsingException;
import javax.sql.rowset.RowSetWarning;

import org.apache.ibatis.javassist.tools.web.BadHttpRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.View;

import com.spring.web.view.ExcelView;

@Controller
@RequestMapping("/exception")
public class TestExceptionController {

	/*
	 * 如果路径变量被设置到要捕获的路径的最后，注意@PathVariable捕获的路径变量的值不会包含请求url最后的后缀名
	 * 
	 * 例如：本方法中，路径变量{ex}在要捕获的路径"/throw/{ex}"的最后，假设请求为/throw/hello.js，
	 * 那么ex路径变量捕获的值是hello，而不是hello.js，即忽略了请求的后缀名。
	 */
	@GetMapping("/throw/{ex}")
	public void exception1(@PathVariable("ex") Class<? extends Exception> exceptionClass) throws Exception {
		Exception exception = BeanUtils.instantiate(exceptionClass);
		throw exception;
	}

	/*
	 * 测试DefaultHandlerExceptionResolver对spring系列的异常的处理
	 * 
	 * 注意： HandlerExceptionResolver的四个具体实现类中，
	 * 使用ResponseStatusExceptionResolver和DefaultHandlerExceptionResolver处理异常后，
	 * 就只是对响应的状态码和错误原因进行设置，不会进行后续的视图渲染等工作。
	 */
	@GetMapping("/throw/default")
	public void testDefaultException() throws HttpMessageNotWritableException {
		HttpMessageNotWritableException ex = new HttpMessageNotWritableException("test");
		throw ex;
	}

	/*
	 * 当当前类中有两个@ExceptionHandler方法可以处理抛出的异常时，spring会选择最接近抛出的异常的类型的@
	 * ExceptionHandler方法来进行异常处理。而如果发现当前类中有多于一个的最接近抛出的异常的类型的@ExcpetionHandler，
	 * 那么spring则会报错
	 */
	@ExceptionHandler
	@ResponseStatus(code = HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
	public String handleCertificateException(CertificateException ex, Model model) {
		model.addAttribute("time", new Date());
		return "basic/dept";
	}

	/*
	 * 即使在@ExceptionHandler注解的value属性中设置了要处理的异常类型，
	 * 在@ExceptionHandler所注解的方法中也可以设置对应异常类型的参数。
	 * 但此时@ExceptionHandler方法中的异常类型必须是@ExceptionHandler注解中所有异常类型的共同的父类
	 * 
	 * 例如本例中ExceptionHandler注解的value属性值是CertificateParsingException和CertificateNotYetValidException，
	 * 代表该方法可以处理这两个异常类型。而@ExceptionHandler方法的异常类型是CertificateException，
	 * 是那两个类共同的父类。以此类推，该@ExceptionHandler方法的异常类型还可以是Exception
	 */
	@ExceptionHandler({ CertificateParsingException.class, CertificateNotYetValidException.class })
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public void handleCertificateNotYetValidException(PrintWriter writer, CertificateException ex) {
		writer.println("发生了异常，错误堆栈如下：");
		ex.printStackTrace(writer);
	}

	/*
	 * 如果在@ExceptionHandler方法中又再一次抛出了异常，
	 * 那么则认为ExceptionHandlerExceptionResolver无法处理该异常，
	 * 转而让下一个HandlerExceptionResolver来处理这个异常
	 */
	@ExceptionHandler
	@ResponseStatus(code = HttpStatus.INSUFFICIENT_STORAGE)
	public void handleHttpMessageNotWritableException(HttpMessageNotWritableException ex) {
		throw ex;
	}

	@ExceptionHandler
	@ResponseBody
	@ResponseStatus(code = HttpStatus.CONFLICT)
	public SQLException handleSqlException(SQLException exception) {
		SQLException newEx = new SQLException("hello test", exception);
		return newEx;
	}

	@ExceptionHandler(RowSetWarning.class)
	// 如果响应状态码是4xx或5xx，那么浏览器是不会弹出下载文件下载对话框的，反而会报“找不到文件”的错误提示
	// @ResponseStatus(code = HttpStatus.BAD_REQUEST)
	@ResponseStatus(code = HttpStatus.MULTI_STATUS)
	public View handleRowSetWarning(Model model) {
		model.addAttribute("hello", "你好");
		model.addAttribute("world", "世界");
		model.addAttribute(new Date());

		View view = new ExcelView();
		return view;
	}

	@ExceptionHandler
	@ResponseStatus(code = HttpStatus.EXPECTATION_FAILED)
	@ModelAttribute
	public BadHttpRequest handleBadHttpRequest(BadHttpRequest ex) {
		return ex;
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	public static class HelloException extends Exception {

	}

	/*
	 * 只有当ResponseStatusExceptionResolver解析异常类上的@ResponseStatus时，
	 * 才会将reason属性值丢给MessageSource，获取该code对应的内容
	 * 在请求处理方法或@ExceptionHandler方法中写@ResponseStatus，都是直接使用reason属性值，
	 * 不经过MessageSource处理
	 */
	@ResponseStatus(code = HttpStatus.GATEWAY_TIMEOUT, reason = "exception.reason")
	public static class WorldException extends Exception {

	}
}
