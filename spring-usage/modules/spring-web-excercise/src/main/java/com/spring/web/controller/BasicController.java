package com.spring.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.spring.data.domain.BaseDept;
import com.spring.web.converter.BaseDeptFormatter;

@Controller
// 在@RequestMapping（包括@GetMapping等）中，可以使用ant路径。
@RequestMapping("/basi?*")
@SessionAttributes(names = { "hello", "world" }, types = { BaseDept.class, GregorianCalendar.class })
public class BasicController {

	@Resource(name = "dept")
	private BaseDept baseDept;

	@SuppressWarnings("unused")
	@Autowired
	private String hello, world;

	@SuppressWarnings({ "deprecation", "rawtypes" })
	@GetMapping("/view/{view}")
	public String viewName(@PathVariable String view,
			@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss") @RequestParam Date date, Model model,
			@RequestParam("class") Class clazz) {
		model.addAttribute(date);
		if (date.getMonth() == new Date().getMonth()) {
			throw new RuntimeException("sdf");
		}
		return view;
	}

	/*
	 * 如果要使用我们自定义的Converter来将请求参数的值转换成我们需要的对象，那么必须在请求处理方法的参数上明确给出@RequestParam或@
	 * PathVariable注解。
	 */
	@RequestMapping("/converter/{dept}")
	public void testDeptConverter(@PathVariable BaseDept dept, PrintWriter outer) {
		outer.print(dept.getDeptName());
	}

	/*
	 * 关于@ModelAttribute注解在参数中，有一个细节就是：当无法从Model中获取key为baseDept的value对象时，
	 * spring会创建一个BaseDept类型的对象，并注册到Model中。这时假如BaseDept为一个接口，则无法创建实例，则会报错。
	 * 因此，当没有把握能从Model中获取value，那么就尽量把@ModelAttribute所注解的类型设置为具体类，而不是接口。
	 */
	// @RequestMapping中params属性的四种格式
	@RequestMapping(path = "/testModel/{deptNo}/{deptCode}", params = { "deptName=test_dept", "modifyDate",
			"isRun!=off", "!deptNo" })
	public void testModel(@ModelAttribute BaseDept dept, PrintWriter outer) {
		outer.print(dept.getDeptName());
	}

	/*
	 * 注意：@RequestParam("deptParam") BaseDept queryDept和BaseDept queryDept的区别是：
	 * 
	 * @RequestParam("deptParam") BaseDept queryDept是从请求参数中获取参数名为deptParam的参数值，
	 * 并将该参数值使用WebDataBinder来转换成参数的类型BaseDept。
	 * 
	 * BaseDept queryDept则相当于@ModelAttribute BaseDept
	 * queryDept，即是从Model中获取对应的BaseDept的实例，然后使用请求参数或路径变量来为这个BaseDept对象的属性赋值
	 */
	/*
	 * 当@ModelAttribute注解在@RequestMapping方法上时，spring会把方法的返回值注册到Model中。至于逻辑视图名，
	 * 则是根据当前请求的url来获得。例如请求的url为/spring-web/dispatcher/basic/dept.htm，
	 * spring会先去掉web项目名spring-web，再去掉访问DispatcherServlet的url-
	 * pattern的前缀dispatcher，最后去掉后缀名.htm，最终获得逻辑视图名basic/dept。
	 */
	@GetMapping(path = "/dept", params = "deptParam")
	@ModelAttribute
	public BaseDept testModelRequestMapping(@RequestParam("deptParam") BaseDept queryDept) {
		return queryDept;
	}

	@GetMapping(path = "/sessionAttributes")
	public void testSessionAttributes(ModelMap model, @ModelAttribute("dept") BaseDept dept,
			@SessionAttribute(name = "calendar", required = false) Calendar calendar, PrintWriter out) {
		model.forEach((name, value) -> out.println(name + ":" + value));
		model.addAttribute("calendar", Calendar.getInstance());
	}

	// 在请求头中Upgrade-Insecure-Requests的值是1，由于参数类型是Boolean，因此被转换成true
	// 像@PathVariable,@CookieValue,@RequestHeader,@RequestParam这些，都会使用DataBinder将对应的值转换成参数的类型
	@GetMapping(path = "/testHeader", headers = { "Accept", "Connection" }, params = { "test", "param" })
	public void testRequestHeader(@RequestHeader("Connection") String connection,
			@RequestHeader("Accept") String[] acceptArray, @RequestHeader("Upgrade-Insecure-Requests") Boolean reuqests,
			@RequestParam char[] test, @RequestParam byte[] param, @RequestHeader Map<String, String> headerMap,
			PrintWriter out) {
		out.println(connection);
		out.println(ArrayUtils.toString(acceptArray));
		out.println(reuqests);

		headerMap.forEach((name, value) -> out.println(name + ":" + value));
	}

	@GetMapping("/cookie")
	public void testCookie(HttpServletResponse response,
			@CookieValue(name = "hello", required = false) String helloCookie, String test,
			@CookieValue(name = "JSESSIONID", required = false) String cookieSessionId) throws IOException {
		if (StringUtils.hasText(test)) {
			Cookie cookie = new Cookie("hello", test);
			response.addCookie(cookie);
		}

		response.getWriter().print(StringUtils.hasText(helloCookie) ? helloCookie : test);
	}

	/*
	 * 使用redirect进行跳转的细节总结：
	 * 
	 * 1.在请求跳转前，不能使用response进行响应输出
	 * 
	 * 2.在请求处理方法中的Model中的内容会作为要跳转的路径的请求参数
	 * 
	 * 3.在访问的地址中的请求参数不会自动被补充到要跳转的路径的请求参数中
	 * 
	 * 4.redirect的地址的写法，以“/”开头是相对于当前web项目，不以"/"开头是相对于当前请求路径的父目录
	 * 
	 * 5.spring不会自动对当前请求处理方法中的Model的内容和跳转后的请求的请求处理方法中的Model的内容进行同步
	 */
	@GetMapping("/redirect")
	@ResponseStatus(code = HttpStatus.TEMPORARY_REDIRECT)
	public String testRedirect(Model model, String query, String lala, PrintWriter out) {

		/*
		 * 1.注意：如果已经使用ServletResponse进行响应，并将响应内容输出到目的段后，再使用"redirect:"进行请求跳转的话，
		 * 则会报错Cannot call sendRedirect() after the response has been committed
		 */
		// out.print("test");
		// out.flush();

		/*
		 * 2.有一点需要注意的是：
		 * 在使用redirect前缀进行路径跳转时，会把当前请求处理方法的Model中的内容作为请求参数，放置在要跳转的路径的后面。
		 * 
		 * 假设当前请求处理方法的Model中的内容为{"param":"haha","hello":"world"}，
		 * 使用"redirect:"前缀要跳转的路径为"/spring-web-excercise/dispatcher/images/1.png"
		 * ，那么在实际跳转的url中，会把Model中的内容放置到请求路径的后面，作为请求参数存在。
		 * 即跳转的路径为"/spring-web-excercise/dispatcher/basic/redirect?param=haha&hello=world"
		 */
		model.addAttribute("param", "haha");

		/*
		 * 3.但是： 在当前请求中的请求参数，如果没有存储到Model中，spring不会在要跳转的url中也带上这些请求参数。
		 * 例如：请求的路径是/spring-web/dispatcher/basic/redirect?hello=zhangsan，要跳转的路径是
		 * /spring-web/dispatcher/images/1.png，如果没有将请求参数hello存储到Model中，
		 * 那么跳转的路径后面是不会带有hello=zhangsan的请求参数的。
		 * 
		 * 下面这行把请求参数的值放到Model中，为了让在请求跳转时，在跳转的地址中可以带上该参数。
		 */
		model.addAttribute("query", query);

		/*
		 * 4.redirect中的地址可以有两种写法：
		 * 
		 * 1.不以"/"开头。那么redirect给出的路径是基于当前请求路径的父目录的。
		 * 例如当前请求路径为"/spring-web-excercise/dispatcher/basic/redirect"。
		 * 这样请求路径的父目录则是"/spring-web-excercise/dispatcher/basic"，那么实际要跳转的路径则是
		 * "/spring-web-excercise/dispatcher/basic/images/1.png"
		 * 
		 * 2.以"/"开头。那么redirect给出的路径是基于当前web项目根目录的。例如路径为"redirect:/images/1.png"，
		 * 假设web项目的根目录为spring-web-excercise，
		 * 那么实际要跳转的路径是"/spring-web-excercise/images/1.png"
		 * 
		 * 注意：要跳转的路径不是基于当前DispatcherServlet的url-pattern的，因此，
		 * 如果想要跳转后的地址还被DispatcherServlet处理，那么需要在要跳转的路径上加上DispatcherServlet的url-
		 * pattern的前缀。例如DispatcherServlet的url-pattern为/dispatcher/*，
		 * 那么当要跳转的路径也需要该DispatcherServlet来处理时，
		 * 需要修改跳转的路径为"redirect:/dispatcher/images/1.png"
		 */
		return "redirect:receiveRedirect";
	}

	/*
	 * 5.还有一点是，假设处理当前请求的请求处理方法是A，处理跳转的请求的请求处理方法是B，
	 * 那么spring不会自动把方法A中Model的内容同步到方法B的Model中
	 */
	@RequestMapping(path = "/receiveRedirect")
	/*
	 * 参数lala没有值，说明原请求中的参数不会被带入到跳转的请求的参数中。参数hello有值，说明原请求处理方法中的Model中的内容，
	 * 会作为请求参数补充到跳转的请求中。
	 */
	public void receiveRedirect(ModelMap model, String hello, String lala, PrintWriter writer) {
		writer.println("hello:" + hello);
		writer.println("lala:" + lala);
	}

	/*
	 * forward的使用细节：
	 * 
	 * 1.在进行请求跳转之前，也可以使用ServletResponse进行响应输出（这点和redirect不同）
	 * 
	 * 2.在请求处理方法中的Model中的内容，不会补充到要跳转的地址的请求参数中（这点和redirect不同）
	 * 
	 * 3.当前请求中的请求参数，可以被处理跳转后的请求的请求处理方法来获取（这点和redirect不同）
	 * 
	 * 4.forward地址的两种写法和redirect相同
	 * 
	 * 5.spring不会自动对当前请求处理方法中的Model的内容和跳转后的请求的请求处理方法中的Model的内容进行同步（
	 * 这点和redirect相同）
	 */
	@GetMapping("/forward")
	public String testForward(Model model, String query, PrintWriter writer) {

		writer.println("test");
		writer.flush();

		model.addAttribute("person", "zhangsan");
		return "forward:receiveForward";
	}

	@GetMapping(path = "/receiveForward")
	/*
	 * 参数query有值，说明原请求中的参数会记录到跳转后的请求的参数中。参数person没有值，
	 * 说明原请求处理方法中的Model中的内容不会补充到跳转的请求的参数中
	 */
	public void receiveForward(Model model, String query, String person, PrintWriter writer) {
		writer.println("query:" + query);
		writer.println("person:" + person);
	}

	// 在使用HttpMessageConverter向目的端输出时，是使用ContentNegotiationManager来获取要响应的媒体类型的
	@GetMapping("/responseBody")
	@ResponseBody
	public BaseDept testResponseBody(@ModelAttribute("dept") BaseDept dept, @ModelAttribute("hello") String hello,
			@ModelAttribute("world") String world) {
		dept.setDeptCode(hello);
		dept.setDeptName(world);
		return dept;
	}

	@PostMapping("/requestBody")
	@ResponseBody
	// 在使用HttpMessageConverter来将请求报文中的内容转换成对象时，是根据请求头中的Content-Type属性来获取请求报文的内容的媒体类型的
	// 在确定了媒体类型和要转换或输出的java类型后，就可以确定要使用哪个HttpMessageConverter来进行处理了
	public BaseDept testRequestBody(@RequestBody BaseDept dept) {
		return dept;
	}

	/*
	 * 向目的端输出内容有三种方式：
	 * 
	 * 1.使用HttpMessageConverter（此时需要在请求处理方法中加上@ResponseBody注解）
	 * 2.使用ServletResponse在请求处理方法中直接输出（此时需要请求处理方法的返回值类型为void）
	 * 3.使用View对象进行输出（此时请求处理方法需要返回String类型的逻辑视图名或View对象或ModelAndView对象）
	 */
	@PostMapping("/requestBody1")
	public void testRequestBody1(@RequestBody byte[] requestBody, OutputStream out) throws IOException {
		StreamUtils.copy(requestBody, out);
	}

	// 如果请求处理方法只给出了@ResponseStatus，那么此时则相当于<mvc:status-controller>，只会输出一个响应码
	@RequestMapping("/testStatus")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public void testResponseStatus() {
	}

	@GetMapping("/reason")
	public String testReason(@RequestParam Class<?> clazz)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		if (Exception.class.isAssignableFrom(clazz)) {
			// 抛出该异常是为了让handlerNullPointerException这个@ExceptionHandler来处理
			if (NullPointerException.class.isAssignableFrom(clazz)) {
				NullPointerException exception = (NullPointerException) ConstructorUtils.invokeConstructor(clazz);
				throw exception;
			}
			// 将请求跳转到另一个地址来处理
			return "forward:testReason";
		} else {
			return "test";
		}
	}

	// 如果在@ResponseStatus中设置了reason属性的值，那么会直接按照reason属性的值输出，不会按照请求处理方法的返回值来输出
	// 可以看到，当使用该请求处理方法时，响应的内容是@ResponseStatus里的reason属性值，而不是请求处理方法返回的值
	@GetMapping("/testReason")
	@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "错误的请求内容")
	@ResponseBody
	public String testResponseReason() {
		return "hahawawa";
	}

	@ResponseBody
	@RequestMapping("/conversion/{date}")
	@ResponseStatus(code = HttpStatus.ALREADY_REPORTED)
	public String testConversion(@NumberFormat(style = Style.PERCENT) Double percent,
			@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss@SSS") Calendar date,
			@Value("No:3,123,665!") @NumberFormat(pattern = "No:#,###!") Integer code,
			@Value("#{conversionService}") ConversionService conversionService) {
		Method method = ReflectionUtils.findMethod(this.getClass(), "testConversion", Double.class, Calendar.class,
				Integer.class, ConversionService.class);

		MethodParameter percentParameter = new MethodParameter(method, 0);
		MethodParameter dateParameter = new MethodParameter(method, 1);
		MethodParameter codeParameter = new MethodParameter(method, 2);

		TypeDescriptor percentDescriptor = new TypeDescriptor(percentParameter);
		TypeDescriptor dateDescriptor = new TypeDescriptor(dateParameter);
		TypeDescriptor codeDescriptor = new TypeDescriptor(codeParameter);
		TypeDescriptor stringDescriptor = TypeDescriptor.valueOf(String.class);

		String percentStr = (String) conversionService.convert(percent, percentDescriptor, stringDescriptor);
		String dateStr = (String) conversionService.convert(date, dateDescriptor, stringDescriptor);
		String codeString = (String) conversionService.convert(code, codeDescriptor, stringDescriptor);

		StringJoiner joiner = new StringJoiner(",");
		String contents = joiner.add(percentStr).add(dateStr).add(codeString).toString();

		return contents;
	}

	@RequestMapping(path = "/conversion1/{date}", params = "money")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.NON_AUTHORITATIVE_INFORMATION)
	public String testConversion1(@RequestParam @NumberFormat(pattern = "人民币：#,###.#") Double money,
			@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss") Long date,
			@Value("第13,127,989号！") @NumberFormat(pattern = "第#,###号！") Integer code,
			@Value("#{@conversionService}") ConversionService conversionService) {
		Method method = ReflectionUtils.findMethod(this.getClass(), "testConversion1", Double.class, Long.class,
				Integer.class, ConversionService.class);

		MethodParameter moneyParameter = new MethodParameter(method, 0);
		MethodParameter dateParameter = new MethodParameter(method, 1);
		MethodParameter codeParameter = new MethodParameter(method, 2);

		TypeDescriptor moneyDescriptor = new TypeDescriptor(moneyParameter);
		TypeDescriptor dateDescriptor = new TypeDescriptor(dateParameter);
		TypeDescriptor codeDescriptor = new TypeDescriptor(codeParameter);
		TypeDescriptor stringDescriptor = TypeDescriptor.valueOf(String.class);

		String moneyStr = (String) conversionService.convert(money, moneyDescriptor, stringDescriptor);
		String dateStr = (String) conversionService.convert(date, dateDescriptor, stringDescriptor);
		String codeStr = (String) conversionService.convert(code, codeDescriptor, stringDescriptor);

		return new StringJoiner("\n").add(moneyStr).add(dateStr).add(codeStr).toString();
	}

	@RequestMapping(path = "/conversionMap/{param1}/{param2}", params = { "name", "code" })
	public void testConversionMap(@PathVariable Map<String, String> pathMap,
			@RequestHeader Map<String, String> headerMap, @RequestParam Map<String, String> paramMap,
			PrintWriter writer) {
		BiConsumer<String, String> mapShower = (name, value) -> writer.println(name + ":" + value);

		writer.println(org.apache.commons.lang3.StringUtils.center("pathMap", 50, "="));
		pathMap.forEach(mapShower);

		writer.println(org.apache.commons.lang3.StringUtils.center("headerMap", 50, "="));
		headerMap.forEach(mapShower);

		writer.println(org.apache.commons.lang3.StringUtils.center("paramMap", 50, "="));
		paramMap.forEach(mapShower);
	}

	// 当@ModelAttribute方法注解在普通方法时，在方法中也可以使用和请求处理方法相同的注解（例如@RequestParam,@SessionAttribute等）
	@ModelAttribute("time")
	public String time(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss") Date date) {
		return date == null ? "" : DateFormat.getDateTimeInstance().format(date);
	}

	/*
	 * 在@ExceptionHandler的方法的参数和返回值类型中，也可以使用部分注解和参数类型，例如@SessionAttribute、@
	 * RequestAttribute、HttpServletRequest、@ResponseBody等。但不能像请求处理方法那样使用全部的注解，
	 * 例如@RequestParam注解就无法使用。
	 * 
	 * 另外：@ExceptionHandler方法中使用的Model和请求处理方法中的Model不是同一个对象。
	 * 也就是说在调用一个请求处理方法发生异常时，在这个请求处理方法中使用的Model对象和处理这个异常的@ExceptionHandler方法，
	 * 使用的是不同的Model对象。
	 */
	@ExceptionHandler
	@ResponseStatus(code = HttpStatus.BAD_GATEWAY)
	public String handleRuntimeException(RuntimeException ex, HttpServletRequest request,
			@SessionAttribute(required = false, name = "dept") BaseDept dept, Model model) {
		ex.printStackTrace();
		model.addAttribute(dept);
		return "err";
	}

	// 可以通过在@ExceptionHandler方法上增加@ResponseStatus，来直接给出错误响应的状态吗和错误内容
	@ExceptionHandler(NullPointerException.class)
	@ResponseStatus(code = HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, reason = "请求处理发生错误")
	public void handlerNullPointerException() {
	}

	@InitBinder
	public void initDataBinder(DataBinder dataBinder) {
		dataBinder.addCustomFormatter(new BaseDeptFormatter());
	}

}