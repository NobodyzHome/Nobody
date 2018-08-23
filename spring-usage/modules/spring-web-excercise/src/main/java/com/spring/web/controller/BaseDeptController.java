package com.spring.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.data.domain.BaseDept;
import com.spring.data.domain.BaseLine;
import com.spring.data.validation.group.Insert;
import com.spring.data.validation.group.Update;

@Controller
@RequestMapping(path = "/dept")
public class BaseDeptController {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private Environment environment;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = { "!" + MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	public BaseDept receiveRequestBody(@RequestBody BaseDept dept) {
		dept.setModifyDate(new Date());
		return dept;
	}

	@GetMapping(path = "/{query1}/{query2}")
	public void queryLines(@PathVariable String query1,
			@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date query2,
			@MatrixVariable(pathVar = "query1", name = "code") String deptCode,
			@MatrixVariable(pathVar = "query1", name = "no") @NumberFormat(pattern = "No:#") Integer deptNo,
			@MatrixVariable(pathVar = "query2") @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss") List<Date> modifyDates,
			@MatrixVariable @NumberFormat(pattern = "@#") Integer[] deptRange,
			@MatrixVariable Map<String, String> allMatrixVariables,
			@MatrixVariable(pathVar = "query1") Map<String, String> pathMatrixVariables, PrintWriter out) {
		out.println(query1);
		out.println(DateFormat.getDateInstance().format(query2));
		out.println(deptCode);
		out.println(deptNo);
		out.println(ArrayUtils.toString(deptRange));
		out.println(allMatrixVariables);
		out.println(pathMatrixVariables);
	}

	/*
	 * 1.如果请求处理方法的参数没有被任何注解，例如BaseDept
	 * dept，那么相当于对这个参数增加了@ModelAttribute或@RequestParam注解，具体增加哪个注解取决于环境，在
	 * 当前环境中是相当于增加@ModelAttibute注解。
	 * 
	 * 注：如果BaseDept dept相当于增加@RequestParam注解，那么@RequestParam注解的required属性值为false
	 * 
	 * 2.对于@ModelAttribute BaseDept dept，会经历以下操作流程：
	 * 
	 * a) spring会尝试从model中获取key为baseDept的值，如果没有获取到则创建一个BaseDept的实例，
	 * 如果获取到了则使用这个实例。
	 * 
	 * b) 检查路径变量deptNo，发现在BaseDept类中有对应名称的属性，于是就会将捕获的路径变量
	 * deptNo的值按照BaseDept类的deptNo属性上的@NumberFormat注解中提供的格式，转换成对应的Integer类型的属性，
	 * 赋值给BaseDept实例的deptNo属性。依次类推检查其他路径变量deptCode。
	 * 
	 * c) 检查请求参数deptName，发现在BaseDept类中有对应名称的属性，
	 * 于是就会将该请求参数的值转换成BaseDept类的deptName属性的String类型的值，
	 * 并赋值给BaseDept实例的deptName属性。依次类推检查其他请求参数isRun。
	 * 
	 * 3.如果没有在@ModelAttribute BaseDept dept前面加@Valid注解，那么spring不会对这个参数的值自动进行验证，
	 * 而如果加上了@Valid注解，那么spring则会自动对该参数的值进行验证。如果验证结果中有错误，那么dept参数后面
	 * 必须紧挨着一个BindingResult类型的参数，用于存放dept参数的验证结果。如果没有该参数则会报错。
	 * 
	 * 4.不是所有参数被加上@Valid注解后就会被自动验证。只有在@ModelAttribute,@RequestBody,@
	 * RequestPart这些注解的参数上增加@Valid注解，才会进行自动验证。
	 * 
	 * 5.如果只想对某一个分组进行验证，那么使用@Validated代替@Valid注解，可以在@Validated注解的value属性中给出
	 * 要验证的分组️
	 */
	@GetMapping(path = "/validation/modelAttribute/{deptNo}/{deptCode}/{condition}", params = { "deptName", "isRun" })
	public void validateModelAttribute(@Valid BaseDept dept, BindingResult deptValidationResult,
			@MatrixVariable(pathVar = "condition") @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss") Date modifyDate,
			Locale locale, PrintWriter out) {
		deptValidationResult.getFieldErrors()
				.forEach(fieldError -> out.println(messageSource.getMessage(fieldError, locale)));
	}

	@PostMapping(path = "/validation/requestBody", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void validateRequestBody(@Valid @RequestBody BaseDept dept, BindingResult deptValidationResult,
			Locale locale, PrintWriter out) {
		deptValidationResult.getFieldErrors()
				.forEach(fieldError -> out.println(messageSource.getMessage(fieldError, locale)));
	}

	/*
	 * 支持文件上传的话需要有以下配置
	 * 
	 * 1.请求处理方法中的参数可以用@RequestParam注解或@RequestPart注解，用于接收多部分中的某一部分的数据。
	 * 使用@RequestParam注解时，参数类型必须是MultipartFile或Part类型的。而使用@RequestPart注解时，
	 * 参数除了上面那两种类型以外，还可以是HttpMessageConverter能处理的类型的，因为使用@RequestPart时，
	 * 会使用HttpMessageConverter，将该部分的内容转换成对应类型的对象。
	 * 
	 * 注意：如果使用@RequestParam时，@RequestParam注解不能省略。例如参数只能是@RequestParam Part
	 * file1，而不能是Part file1。
	 * 
	 * 2.在DispatcherServlet的容器配置文件中配置MultipartResolver，并且标识符必须为multipartResolver
	 *
	 * 3.如果使用StandardServletMultipartResolver，需要在web.xml中DispatcherServlet那个<
	 * serlvet>中增加<multipart-config>配置
	 */
	@PostMapping(path = "/validation/requestPart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public void validateRequestPart(@Valid @RequestPart(name = "json") BaseDept deptFromJson,
			BindingResult deptJsonValidationResult, @Valid @RequestPart(name = "xml") BaseDept deptFromXml,
			BindingResult deptXmlBindingResult, Locale locale, PrintWriter out) {
		deptJsonValidationResult.getFieldErrors()
				.forEach(fieldError -> out.println(messageSource.getMessage(fieldError, locale)));

		out.println(StringUtils.center("分割线", 50, "="));

		deptXmlBindingResult.getFieldErrors()
				.forEach(fieldError -> out.println(messageSource.getMessage(fieldError, locale)));
	}

	@GetMapping(path = "/testDownload")
	@ResponseBody
	public Resource testDownload(HttpServletResponse response,
			@Value("classpath:spring/container/springMvc-servlet.xml") Resource resource) throws IOException {
		// 设置该响应头的目的是为了告诉浏览器，浏览器不要将收到的响应数据显示到页面，而是需要弹出一个下载对话框，将收到的响应数据下载到客户端的本地文件中
		response.setHeader("Content-Disposition", "attachment;fileName=" + resource.getFilename());
		return resource;
	}

	@PostMapping("/uploadAndDownload")
	public String uploadAndDownload(@RequestParam Part file1, @Value("classpath:") Resource classpath, Model model)
			throws IOException {
		String fileName = file1.getSubmittedFileName();
		File destination = new File(classpath.getFile(), fileName);
		FileUtils.copyInputStreamToFile(file1.getInputStream(), destination);

		String path = environment.resolveRequiredPlaceholders("${urlPrefix}/secret/" + fileName);
		model.addAttribute("path", path);

		return "views/uploadAndDownload";
	}

	@PostMapping(path = "/validation/group", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	public void testValidateGroup(@Validated({ Insert.class, Update.class }) @RequestBody BaseLine line,
			BindingResult lineResult, Locale locale, PrintWriter out) {
		lineResult.getFieldErrors().forEach(fieldError -> out.println(messageSource.getMessage(fieldError, locale)));
	}

	@PostMapping(path = "/validation/group/sequence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public void testValidationSequence(@Valid @RequestPart(name = "file1") BaseLine line, BindingResult lineResult,
			Locale locale, PrintWriter out) {
		lineResult.getFieldErrors()
				.forEach(fieldError -> out.println(messageSource.getMessage(fieldError, locale) + "   " + fieldError));
	}

}