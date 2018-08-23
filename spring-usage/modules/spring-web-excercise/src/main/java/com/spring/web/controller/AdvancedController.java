package com.spring.web.controller;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.spring.data.domain.BaseDept;

@Controller
@RequestMapping(path = "/advanced")
@ResponseStatus(code = HttpStatus.ALREADY_REPORTED)
public class AdvancedController {

	@Value("2017年10月20日 20时15分30秒")
	@DateTimeFormat(pattern = "yyyy年MM月dd日 kk时mm分ss秒")
	private Date date;

	/*
	 * 1.只能获取路径变量捕获的路径下的Matrix参数。以下例来说就是只能获取{dept},{line}
	 * 这两个路径变量所捕获的路径中的Matrix参数的值
	 * 2.使用@PathVariable只是捕获请求路径的部分，而不会捕获该请求路径上的Matrix参数。例如{dept}捕获的请求内容是/depts;
	 * code=test;name=dept，那么@PathVariable("dept")获取的值依然只是"depts"，
	 * 而不是"depts;code=test;name=dept"
	 * 
	 * 3.@MatrixVariable("start")代表获取任意请求片段中，参数名为start的Matrix参数的值
	 * 
	 * @MatrixVariable(pathVar="line",name="start")
	 * 代表获取路径变量line中的Matrix参数名为start的参数值
	 * 
	 * @MatrixVariable Integer start 代表获取任意请求片段中，参数名为start的Matrix参数的值
	 * 
	 * @MatrixVariable(pathVar="line") Integer
	 * start代表获取路径变量line中的Matrix参数为start的参数值
	 */
	@GetMapping("/matrix/depts/{dept}/lines/{line}")
	@ResponseBody
	public String testMatrix(@PathVariable String dept, @PathVariable String line,
			@MatrixVariable(pathVar = "dept", name = "start") Integer deptStart,
			@MatrixVariable(pathVar = "dept", name = "end") Integer deptEnd, @MatrixVariable Integer lineStart,
			@MatrixVariable Integer lineEnd) {
		return deptStart + "," + deptEnd + "," + lineStart + "," + lineEnd;
	}

	/*
	 * 4.可以使用@MatrixVariable注解到Map类型上
	 * 
	 * @MatrixVariable Map<String, String> allMatrixMap代表获取所有路径变量中的所有Matrix参数的值
	 * 
	 * @MatrixVariable(pathVar = "dept") Map<String, String>
	 * deptMatrixMap代表获取路径变量dept所捕获的路径下的所有Matrix参数的值
	 * 
	 * 要注意的一点是：同@RequestParam、@PathVariable注解到Map类型上一样，@MatrixVariable注解在Map类型上后
	 * ，只会获得一个Map<String,String>的对象，spring并没有对Map的value使用ConversionService进行类型转换
	 * ，而是直接把获取的内容作为字符串存放到value中。
	 */
	@GetMapping("/matrixMap/depts/{dept}/lines/{line}")
	public void matrixMap(@MatrixVariable Map<String, String> allMatrixMap,
			@MatrixVariable(pathVar = "dept") Map<String, String> deptMatrixMap,
			@MatrixVariable(pathVar = "line") Map<String, String> lineMatrixMap, PrintWriter out) {
		BiConsumer<String, String> mapShower = (name, value) -> out.println(name + ":" + value);

		out.println(StringUtils.center("allMatrixMap", 50, "="));
		allMatrixMap.forEach(mapShower);

		out.println(StringUtils.center("deptMatrixMap", 50, "="));
		deptMatrixMap.forEach(mapShower);

		out.println(StringUtils.center("lineMatrixMap", 50, "="));
		lineMatrixMap.forEach(mapShower);
	}

	/*
	 * 5.一个Matrix参数可以有多个值，其方式是在url中，将该Matrix参数的值以","分隔。例如/depts;range=2,3,11,56，
	 * 那么代表参数range有多个值，即2和3和11和56。
	 * 在springMvc的请求处理方法中，也可以通过数组类型参数的方式，来获取一个Matrix参数的多个值。
	 * 
	 * @MatrixVariable(pathVar = "dept", name = "valid") int[]
	 * validArray,则是获取路径变量为dept所捕获的路径下的Matrix参数名为valid的所有参数值
	 * 
	 * 注意：
	 * 
	 * a) 这里有一点就需要注意的是，","是一个关键字，只能在用于分隔多个参数值出现，被拆分的某一个参数值中不能单独出现","。例如：
	 * 如果想单独一个参数值是"No,#"的格式，例如/depts;region=No,31,No,55,No,80，想的是获取三个参数值，分别为No,
	 * 31和No,55和No,80，但实际获得的参数值是No和31和No和55和No和80。这是因为"No,31"也是以","分隔的，
	 * 因此也会被进行拆分。因此，多个参数值里的每一个子值不能出现","。
	 * 
	 * b) 一个Matrix参数中有多个值，每个值都会继续被ConversionService转换成目标类型。
	 * 具体内容参见【conversionArray】和【conversionArray】方法
	 */
	@GetMapping("/matrixArray/depts/{dept}")
	public void matrixArray(@PathVariable String dept,
			@MatrixVariable(pathVar = "dept") Map<String, String> deptMatrixMap,
			@MatrixVariable(pathVar = "dept", name = "valid") int[] validArray,
			@MatrixVariable(pathVar = "dept", name = "start") Integer start,
			@MatrixVariable(pathVar = "dept", name = "end") int end, PrintWriter out) {
		out.println(StringUtils.center("deptMatrixMap", 50, "="));
		deptMatrixMap.forEach((name, value) -> out.println(name + ":" + value));
		out.println(ArrayUtils.toString(validArray));
	}

	@RequestMapping("/matrixValue/{dept}/{line}")
	public void matrixValue(@PathVariable String dept, @PathVariable String line,
			@MatrixVariable(pathVar = "dept", name = "start") int deptStart,
			@MatrixVariable(pathVar = "dept", name = "end") int deptEnd,
			@MatrixVariable(pathVar = "line", name = "valid") List<Integer> lineValid, PrintWriter out) {
		out.println(deptStart);
		out.println(deptEnd);
		out.println(lineValid);
	}

	/*
	 * 以下演示了对Matrix参数值的类型转换以及当一个Matrix参数有多个值时，对每一个值都进行类型转换
	 * 
	 * @MatrixVariable(pathVar = "line") @DateTimeFormat(pattern = "yyyy-MM-dd")
	 * Set<Date> modifyDates
	 * 以上面这个参数为例，假设路径片段为/lines;modifyDates=2017-10-10,2019-05-30,2016-03-30,
	 * spring会把modifyDates参数值按照","进行拆分，拆分成2017-10-10和2019-05-30和2016-03-30这三个值，
	 * 然后把这三个值都继续使用ConversionService，按照yyyy-MM-dd的格式转换成Date类型的对象，
	 * 再把这三个转换后的Date对象存储到一个Set对象里。
	 */
	@GetMapping("/matrixConversion/{dept}/{line}")
	@ResponseBody
	public String matrixConversion(
			@MatrixVariable(pathVar = "dept", name = "modifyDate") @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss@SSS") Date deptModifyDate,
			@MatrixVariable @NumberFormat(pattern = "No:#!") Integer lineNo,
			@MatrixVariable(name = "lineNoArray") @NumberFormat(pattern = "No:#") Integer[] array,
			@MatrixVariable(pathVar = "line") @DateTimeFormat(pattern = "yyyy-MM-dd") Set<Date> modifyDates) {

		StringJoiner joiner = new StringJoiner("\n");
		joiner.add(DateFormat.getDateTimeInstance().format(deptModifyDate)).add(lineNo.toString())
				.add(ArrayUtils.toString(array));
		modifyDates.stream().forEach(modifyDate -> joiner.add(DateFormat.getDateInstance().format(modifyDate)));

		return joiner.toString();
	}

	@RequestMapping("/matrixConversion1/{dept}/{line}")
	@ResponseBody
	public String matrixConversion1(@MatrixVariable(pathVar = "dept", name = "code") char[] deptCode,
			@MatrixVariable(pathVar = "line") @NumberFormat(pattern = "No:#!") Integer lineNo,
			@MatrixVariable("modifyDate") @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss@SSS") Date date) {
		StringJoiner joiner = new StringJoiner("\n");
		joiner.add(new String(deptCode)).add(lineNo.toString()).add(DateFormat.getDateTimeInstance().format(date));

		return joiner.toString();
	}

	/*
	 * spring对于任何需要转换成数组类型的参数，都会先把字符串的值按照","进行分割，然后将分割的每一个子字符串，
	 * 使用ConversionService继续进行目标类型的转换。不仅仅是@MatrixVariable注解的参数可以，
	 * 
	 * @PathVaraiable、@RequestParam、@CookieValue等任何使用ConversionService进行类型转换的地方，
	 * 都会进行此操作。
	 * 
	 * 例如：@NumberFormat(pattern = "No:#!") Integer[] codeArray，由于参数类型是数组，
	 * 因此spring则是把请求参数中codeArray参数的值"No:#321!,No:#390!,No:#991!"按","拆分，
	 * 对于拆分成的每个子字符串"No:#321!","No:#390!","No:#991!"，继续使用ConversionService按照
	 * 
	 * @NumberFormat(pattern ="No:#!")的格式转换成目标类型Integer，
	 * 将转换后321,390,991这三个值存放到Integer[]数组中。
	 */
	@GetMapping(path = "/conversionArray/{array}", params = "codeArray")
	@ResponseBody
	public String conversionArray(@PathVariable("array") @DateTimeFormat(pattern = "yyyy-MM-dd") Date[] dateArray,
			@NumberFormat(pattern = "No:#!") Integer[] codeArray,
			@MatrixVariable @NumberFormat(pattern = "No:#") int[] noArray) {
		StringJoiner joiner = new StringJoiner("\n");

		Arrays.stream(dateArray).forEach(date -> joiner.add(DateFormat.getDateTimeInstance().format(date)));
		Arrays.stream(codeArray).forEach(code -> joiner.add(code.toString()));
		Arrays.stream(noArray).forEach(no -> joiner.add(String.valueOf(no)));

		return joiner.toString();
	}

	@GetMapping(path = "/matrixExcercise/{dept}/{line}/{bus}", params = "drivers")
	public void matrixExcercise(@PathVariable String dept, @PathVariable String line, @PathVariable("bus") String buses,
			@MatrixVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date modifyDate,
			@MatrixVariable(pathVar = "dept") @NumberFormat(pattern = "#!") Integer no,
			@MatrixVariable(pathVar = "dept", name = "code") String deptCode,
			@MatrixVariable(pathVar = "line") @NumberFormat(pattern = "No:#") Integer[] range,
			@MatrixVariable(pathVar = "bus", name = "modifyDates") @DateTimeFormat(pattern = "yyyy_MM_dd") List<Date> busModifyDates,
			@MatrixVariable String[] busNames, @MatrixVariable Map<String, String> allMatrixValues,
			@MatrixVariable(pathVar = "bus") Map<String, String> busMatrixValues,
			@NumberFormat(pattern = "@#") Long[] drivers, @RequestHeader("Accept") List<MediaType> acceptArray,
			PrintWriter out) {
		out.println("@PathVariable String dept : " + dept);
		out.println("@PathVariable String line : " + line);
		out.println("@PathVariable(\"bus\") String buses : " + buses);
		out.println("@MatrixVariable @DateTimeFormat(pattern = \"yyyy-MM-dd\") Date modifyDate : "
				+ DateFormat.getDateInstance().format(modifyDate));
		out.println("@MatrixVariable(pathVar = \"dept\") @NumberFormat(pattern = \"#!\") Integer no : " + no);
		out.println("@MatrixVariable(pathVar = \"dept\", name = \"code\") String deptCode : " + deptCode);
		out.println("@MatrixVariable(pathVar = \"line\") @NumberFormat(pattern = \"No:#\") Integer[] range : "
				+ ArrayUtils.toString(range));
		out.println(
				"@MatrixVariable(pathVar = \"bus\", name = \"modifyDates\") @DateTimeFormat(pattern = \"yyyy_MM_dd\") List<Date> busModifyDates : "
						+ ArrayUtils
								.toString(busModifyDates.stream().map(DateFormat.getDateInstance()::format).toArray()));
		out.println("@MatrixVariable String[] busNames : " + ArrayUtils.toString(busNames));
		out.println("@MatrixVariable Map<String, String> allMatrixValues : " + ArrayUtils.toString(allMatrixValues
				.entrySet().stream().map(entry -> "[" + entry.getKey() + "," + entry.getValue() + "]").toArray()));
		out.println("@MatrixVariable(pathVar = \"bus\") Map<String, String> busMatrixValues : "
				+ ArrayUtils.toString(busMatrixValues.entrySet().stream()
						.map(entry -> "[" + entry.getKey() + "," + entry.getValue() + "]").toArray()));
		out.println("@NumberFormat(pattern = \"@#\") Long[] drivers : " + ArrayUtils.toString(drivers));
		out.println("@RequestHeader(\"Accept\") List<String> acceptArray : " + acceptArray);
	}

	/*
	 * 假设dept和line这两个路径变量捕获的路径片段中，都有参数名为code的Matrix参数，例如url为/matrixException/
	 * deptInfo;code=5/lineInfo;code=1，那么下例中，我们直接获取Matrix参数名为code的值时，则会报以下错误，
	 * 因为不知道获取哪个路径片段下的code参数的值
	 * 
	 * Found more than one match for URI path parameter 'code' for parameter
	 * type [java.lang.String]. Use 'pathVar' attribute to disambiguate.
	 * 
	 * 注意： 即使把参数修改为@MatrixVariable String[]
	 * code，也会报错，因为这样是获得某一个路径片段的code参数的多个值，而不是获取所有路径片段中的都名为code参数的值
	 */
	@GetMapping(path = "/matrixException/{dept}/{line}")
	@ResponseBody
	public String matrixException(@MatrixVariable String code) {
		return code;
	}

	/*
	 * consumes属性用于限制请求头的Conetent-Type属性值
	 * 注意：和produces属性一样，如果一个请求的路径和@RequestMapping的path属性相符，但请求头的Content-
	 * Type属性值和consumes属性值不符，那么则会抛出HttpMediaTypeNotSupportedException异常
	 */
	@PostMapping(path = "/testConsumes", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE,
			"text/plain;q=0.7" })
	@ResponseBody
	public String testConsumes(@RequestHeader("Content-Type") MediaType contentType) {
		return contentType.toString();
	}

	/*
	 * 1.produces属性验证的是请求实际期望的媒体类型是否在produces属性中。
	 * 也就是判断ContentNegotiationManager返回的请求所实际期望的媒体类型，而不是只判断请求头中Accept属性所对应的媒体类型
	 * 
	 * 2.如果请求的路径和path属性值相符，但是请求实际期望的媒体类型不在produces属性值中，
	 * 那么则会抛出HttpMediaTypeNotAcceptableException异常，而不是继续寻找请求处理对象。
	 * 
	 * 3.请求实际期望的媒体类型，只要和produces列表中的任意一个相符，即可通过匹配，不需要和produces列表中的全部媒体类型都匹配
	 * 
	 * 4.MediaType类中给出了一些媒体类型的字符串值，例如MediaType.APPLICATION_JSON_VALUE,
	 * MediaType.APPLICATION_XML_VALUE等
	 * 
	 * 5.在produces中，媒体类型中可以使用通配符。例如produces={"application/*"}
	 * 
	 * 6.在produces中，可以使用"!application/xml"的形式来表达请求所期望的实际媒体类型不能是application/xml
	 */
	@GetMapping(path = "/testProduces", produces = { "!application/json", MediaType.APPLICATION_XML_VALUE,
			MediaType.TEXT_PLAIN_VALUE })
	@ResponseBody
	// 注意：可以将字符串形式的媒体类型转换成MediaType类型
	public BaseDept testProduces(@RequestHeader("Accept") MediaType[] acceptMedias) {
		BaseDept dept = new BaseDept(10, "No,10");
		return dept;
	}

	/*
	 * 注意：如果没有配置path，而只配置了consumes或produces，那么就是对当前符合@
	 * Controller类的path限制的请求的请求头和实际期望响应类型进行进一步限制
	 * 
	 * 例如@Controller类上的path属性值是"/advanced"，而该类下的testConsumes1方法的@
	 * RequestMapping没有给出path属性值，而是只给出了consumes="application/json"，produces=
	 * "application/xml"。那么就是当请求的路径是/advanced，同时请求头的Content-Type属性值是application/
	 * json，并且请求实际期望的类型是application/xml时，此时才会由testConsumes1方法来处理这个请求。
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public BaseDept testConsumes1(@RequestHeader(name = "Content-Type") MediaType contentType,
			@RequestBody BaseDept dept) {
		return dept;
	}

	@GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public BaseDept testProduces1() {
		BaseDept dept = new BaseDept(311, "No:311");
		dept.setDeptName("test_dept");
		return dept;
	}

	/*
	 * @RequestMapping的path属性值有以下几点注意
	 * 
	 * 1.path属性值中可以使用ant路径
	 * 
	 * 2.如果path属性值没有给出后缀名，那么即是对请求路径的后缀名没有限制。例如path值为/test/url时，则是符合要求的请求路径为
	 * “/test/ url”，“test/url.htm”，“test/url.js”。而如果path值为/test/url.html，
	 * 那么符合要求的请求路径只能是"/test/url.html"
	 * 
	 * 3.path属性值可以给出一个数组，当请求路径只要符合这个数组里其中一个限制时，即认为请求路径符合path属性值的要求。例如path属性值为
	 * path={"/test/url","/path/*","/?.htm"}，那么请求/test/url，/path/123，
	 * /t.htm都可以通过path属性值的限制。
	 */
	@RequestMapping(path = { "/path/test*/**/?", "/path/**/*", "/test/url" }, consumes = {
			MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public BaseDept testPath(@RequestBody BaseDept dept) {
		dept.setModifyDate(new Date());
		return dept;
	}

	@ExceptionHandler
	@ResponseStatus(code = HttpStatus.GATEWAY_TIMEOUT)
	public void handleException(Exception exception) {
		exception.printStackTrace();
	}
}