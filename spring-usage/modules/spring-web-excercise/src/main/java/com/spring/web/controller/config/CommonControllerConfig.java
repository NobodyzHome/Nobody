package com.spring.web.controller.config;

import org.springframework.web.bind.annotation.ControllerAdvice;

// @ControllerAdvice本身被@Component所注解了，因此在类中就无需再使用@Component单独注解了
@ControllerAdvice
public class CommonControllerConfig {

	/*
	 * 关于使用ControllerAdvice的一个好处是，程序如果在还没有获得请求处理对象时就报异常了的话，
	 * 那么ControllerAdvice中配置的@ExceptionHandler依然有机会处理抛出的异常。
	 * 
	 * 例如：假设ControllerA类中的testProduces方法要求请求的path必须为"/testProduces"，
	 * 同时请求的produces需要为"application/json"，也就是@GetMapping(path = "/testProduces",
	 * produces = "application/json")。
	 * 但如果客户发出的请求中，请求路径符合path属性的值，但是期望的媒体类型不符合produces的值。
	 * 那么则会抛异常，也就是在HandlerMapping尝试根据请求获得HandlerMethod对象时报错。
	 * 这时会将异常交给ExceptionHandlerExceptionResolver处理，但是由于没有获得请求处理对象，
	 * 因此ExceptionHandlerExceptionResolver就无法知道应该使用哪个@Controller类的@
	 * ExceptionHandler方法了，此时ExceptionHandlerExceptionResolver就没有办法处理该异常了。
	 * 但是如果我们设置了@ControllerAdvice，ExceptionHandlerExceptionResolver会优先从该配置中找
	 * 能处理该异常的@ExceptionHandler方法，如果能找到，就直接使用该方法来处理异常了。
	 * 
	 * 总结：在ExceptionHandlerExceptionResolver处理异常时，如果请求处理对象为null，
	 * 那么ExceptionHandlerExceptionResolver就只能使用@ControllerAdvice中的@
	 * ExceptionHandler来处理异常了，无法使用@Controller中配置的@ExceptionHandler。
	 * 此时如果没有配置@ControllerAdvice，那么ExceptionHandlerExceptionResolver就无法处理异常了。
	 */
	// @ExceptionHandler
	// public void handleException(Exception ex, PrintWriter out) {
	// ex.printStackTrace(out);
	// }
}
