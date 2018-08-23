package com.spring.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.sql.rowset.RowSetWarning;
import javax.sql.rowset.serial.SerialException;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.util.WebUtils;

import com.spring.data.domain.BaseDept;
import com.spring.web.view.ExcelView;

@Controller
@RequestMapping("/test/{hello}")
@SessionAttributes(types = BaseDept.class, names = { "action", "viewName" })
public class TestController {

	@ModelAttribute("world")
	public String world() {
		return "世界";
	}

	@ModelAttribute("requestUri")
	public String requestUri(HttpServletRequest request) {
		RequestContext requestContext = new RequestContext(request);
		String uri = requestContext.getRequestUri();

		return uri;
	}

	@GetMapping(path = "/basicPage.htm", params = "view")
	public String page(@PathVariable String hello, String view) {
		return view;
	}

	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE, "!" + MediaType.APPLICATION_XML_VALUE, "text/*" })
	@ResponseBody
	public String getRequestBody(@RequestBody String body, @RequestHeader("content-type") MediaType contentType) {
		return contentType.toString() + "\n" + body;
	}

	@PostMapping(consumes = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public BaseDept requestBody(@RequestBody BaseDept dept) {
		return dept;
	}

	@PostMapping(consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ResponseStatus(code = HttpStatus.ALREADY_REPORTED)
	public BaseDept requestBody1(@RequestBody BaseDept dept) {
		return dept;
	}

	@GetMapping(path = "/matrix/dept/{dept}/show", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public BaseDept matrix(@PathVariable String hello,
			@PathVariable("dept") @NumberFormat(pattern = "No:#") Integer deptNo,
			@MatrixVariable(pathVar = "dept") String deptCode,
			@MatrixVariable(pathVar = "dept", name = "name") String deptName, @MatrixVariable Boolean isRun,
			@MatrixVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss") Date modifyDate) {
		BaseDept dept = new BaseDept(deptNo, deptCode);
		dept.setDeptName(deptName);
		dept.setModifyDate(modifyDate);
		dept.setIsRun(isRun);

		return dept;
	}

	@GetMapping("/matrix/{dept1}/and/{dept2}")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.FORBIDDEN)
	public BaseDept[] matrixExcercise(@PathVariable @NumberFormat(pattern = "@#") Integer[] hello,
			@PathVariable("dept1") @NumberFormat(pattern = "No:#") Integer deptNo1,
			@MatrixVariable(pathVar = "dept1", name = "code") String deptCode1,
			@MatrixVariable(pathVar = "dept1", name = "name") String deptName1,
			@MatrixVariable(pathVar = "dept1", name = "isRun") Boolean isRun1,
			@MatrixVariable(pathVar = "dept1", name = "modifyDate") @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss") Date modifyDate1,
			@PathVariable("dept2") Integer deptNo2, @MatrixVariable(pathVar = "dept2", name = "code") String deptCode2,
			@MatrixVariable(pathVar = "dept2", name = "name") String deptName2,
			@MatrixVariable(pathVar = "dept2", name = "modifyDate") @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss") Date modifyDate2,
			@MatrixVariable(pathVar = "dept2", name = "isRun") Boolean isRun2,
			@MatrixVariable(pathVar = "dept1") Map<String, String> dept1Map,
			@MatrixVariable(pathVar = "dept2") Map<String, String> dept2Map) {

		BaseDept dept1 = new BaseDept(deptNo1, deptCode1);
		dept1.setDeptName(deptName1);
		dept1.setModifyDate(modifyDate1);
		dept1.setIsRun(isRun1);

		BaseDept dept2 = new BaseDept(deptNo2, deptCode2);
		dept2.setDeptName(deptName2);
		dept2.setModifyDate(modifyDate2);
		dept2.setIsRun(isRun2);

		return new BaseDept[] { dept1, dept2 };
	}

	@GetMapping("/view/direct")
	@ResponseStatus(code = HttpStatus.BAD_GATEWAY)
	public View directView(Model model, @PathVariable String hello,
			@Value("lala=xujiaying\nwawa=weiruxuan\nmay=wuyuetian") Properties properties) {
		MappingJackson2JsonView view = new MappingJackson2JsonView();
		view.setAttributes(properties);
		view.setExposePathVariables(true);

		model.addAttribute("time", DateFormat.getTimeInstance().format(new Date()));

		return view;
	}

	@PostMapping("/output/direct")
	public void outputDirect(@RequestBody BaseDept dept, PrintWriter out) {
		out.print(dept.getDeptName() + "(" + dept.getDeptCode() + ")");
	}

	@GetMapping(path = { "/view/resolver", "/views" }, params = "viewName")
	public String viewResolve(@PathVariable String hello, String viewName, Model model, String action) {
		model.addAttribute(new BaseDept()).addAttribute("action", action).addAttribute("viewName", viewName);
		return viewName;
	}

	@RequestMapping(path = "/conversion", params = { "deptNo", "deptCode", "deptName", "modifyDate", "isRun" })
	public void conversion(@RequestHeader("Accept") MediaType[] accepts, @PathVariable char[] hello,
			@RequestParam("nums") @NumberFormat(pattern = "No:#") List<Integer> numberArray,
			@DateTimeFormat(pattern = "yyyyMMdd") HashSet<Date> dateArray,
			@MatrixVariable(pathVar = "hello") @NumberFormat(pattern = "@#") Integer[] values,
			@ModelAttribute BaseDept dept, PrintWriter out) {
		out.println(StringUtils.arrayToCommaDelimitedString(accepts));
	}

	@RequestMapping(path = "/conversion/userdefined")
	@ResponseBody
	public BaseDept conversion1(@RequestParam BaseDept dept) {
		return dept;
	}

	/*
	 * 注意：在Model中存放的@ModelAttribute的验证结果的key是BindingResult.MODEL_KEY_PREFIX
	 * +baseDept，而不是BindingResult.MODEL_KEY_PREFIX +dept。
	 */
	@RequestMapping(path = "/validation/modelAttribute")
	public String validateModelAttribute(@Valid @ModelAttribute BaseDept dept, BindingResult deptResult) {
		return "basicDept";
	}

	/*
	 * 注意：在Model中存放的@RequestBody的验证结果的key是BindingResult.MODEL_KEY_PREFIX
	 * +baseDept，而不是BindingResult.MODEL_KEY_PREFIX +dept。
	 */
	@PostMapping(path = "/validation/requestBody", consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public String validateRequestBody(@Valid @RequestBody BaseDept dept, BindingResult result, Model model) {
		// 注意：@RequestBody获取到的值不会自动添加到Model中，需要手动添加。但@RequestBody的验证结果会自动添加到Model中
		model.addAttribute(dept);
		return "basicDept";
	}

	/*
	 * 在Model中存放的@RequestPart的验证结果的key是BindingResult.MODEL_KEY_PREFIX +
	 * theDept，而不是BindingResult.MODEL_KEY_PREFIX+dept。
	 * 也就是说，在页面<form:form>中modelAttribute属性值应是theDept，而不是dept。
	 * 
	 * 注意：要保证Model中一个对象和这个对象的验证结果具有一致的key，
	 * 例如在model中BaseDept对象的验证结果的key是MODEL_KEY_PREFIX+baseDept，
	 * 那么这个BaseDept对象在Model中的key则必须也是baseDept。这样页面才能获取到这个BaseDept对象的验证结果。
	 */
	@PostMapping(path = "/validation/requestPart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String validateRequestPart(@Valid @RequestPart("theDept") BaseDept dept, BindingResult result, Model model) {
		/*
		 * 由于存放dept参数的验证结果的key是MODEL_KEY_PREFIX+theDept，
		 * 这就迫使dept参数在Model中的key必须是theDept。这样使用<form:form
		 * modelAttribute='theDept'>才能获取到dept参数的验证结果
		 */
		model.addAttribute("theDept", dept);
		return "basicDept";
	}

	@GetMapping(path = "/download", params = "filePath")
	@ResponseBody
	public Resource download(@Value("classpath:") Resource classpath, HttpServletResponse response,
			@RequestParam("filePath") String path) throws IOException {
		Resource resource = classpath.createRelative(path);
		response.addHeader("Content-Disposition", "attachment;filename=" + resource.getFilename());

		return resource;
	}

	@PostMapping(path = "/multipart/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public void upload(Part file1, @RequestParam("file2") MultipartFile secondFile, @RequestPart String file3,
			@RequestPart("file4") BaseDept dept, OutputStream out) throws IOException {
		StreamUtils.copy(file1.getInputStream(), out);
		StreamUtils.copy(secondFile.getBytes(), out);
		StreamUtils.copy(file3.getBytes(), out);
		StreamUtils.copy(dept.getDeptName().getBytes(), out);
	}

	@PostMapping(path = { "/multipart/uploadThenDownload",
			"/upThenDown" }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public byte[] uploadThenDownload(Part file1, @RequestParam("file2") MultipartFile secondFile,
			@RequestPart byte[] file3, @RequestPart("file4") String forthFile, HttpServletResponse response)
			throws IOException {
		response.addHeader("Content-Disposition", "attachment;filename=merged.txt");

		byte[][] allContents = { StreamUtils.copyToByteArray(file1.getInputStream()), secondFile.getBytes(), file3,
				forthFile.getBytes() };

		ArrayList<Byte> result = Arrays.stream(allContents).filter(Objects::nonNull).collect(ArrayList<Byte>::new,
				(container, element) -> container.addAll(Arrays.asList(ArrayUtils.toObject(element))),
				ArrayList::addAll);

		return ArrayUtils.toPrimitive(result.toArray((Byte[]) Array.newInstance(Byte.class, 0)));
	}

	@PostMapping(path = { "/multipart/uploadThenShow", "/upThenShow" })
	public String uploadThenShow(MultipartFile file1, MultipartFile file2, MultipartFile file3, MultipartFile file4,
			@Value("classpath:") Resource classpath, Model model, @ModelAttribute("viewName") String viewName,
			HttpServletRequest request, @Value("false") boolean useClasspath) throws IOException {
		MultipartFile[] fileArray = { file1, file2, file3, file4 };
		AtomicInteger number = new AtomicInteger(0);

		File folder = useClasspath ? classpath.getFile()
				: new File(WebUtils.getRealPath(request.getServletContext(), "WEB-INF/images"));

		Arrays.stream(fileArray).filter(Objects::nonNull)
				.filter(file -> MediaType.valueOf("image/*").isCompatibleWith(MediaType.valueOf(file.getContentType())))
				.forEach(file -> {
					String fileName = file.getOriginalFilename();
					File fileToUpload = new File(folder, fileName);
					try {
						FileUtils.writeByteArrayToFile(fileToUpload, file.getBytes());
						model.addAttribute("img" + number.addAndGet(1),
								"/" + (useClasspath ? "secret" : "images") + "/" + fileName);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});

		return viewName;
	}

	@GetMapping(path = "/exception", params = "class")
	public void testException(@RequestParam("class") Class<? extends Exception> clazz, @PathVariable String hello)
			throws Exception {
		Exception ex = ConstructorUtils.invokeConstructor(clazz);
		throw ex;
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String handleSQLException(SQLException sqlException, Model model) {
		model.addAttribute(sqlException);
		return "views_JsonView";
	}

	@ExceptionHandler({ RowSetWarning.class, SerialException.class })
	public void handleSpecificSQLException(SQLException ex, PrintWriter out) {
		ex.printStackTrace(out);
	}

	@ExceptionHandler(NullPointerException.class)
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public View handleNullPointerException(NullPointerException ex, Model model) {
		model.addAttribute(ex);
		return new ExcelView();
	}

	@ExceptionHandler
	@ResponseBody
	@ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
	public IllegalStateException handleIllegalStateException(IllegalStateException ex) {
		return ex;
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(code = HttpStatus.HTTP_VERSION_NOT_SUPPORTED, reason = "请求处理过程中发生运行错误")
	public void handleRuntimeException() {
	}

	@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "exception.reason.hello")
	public static class HelloException extends Exception {
	}

	@ResponseStatus(code = HttpStatus.CONFLICT, reason = "exception.reason.world")
	public static class WorldException extends Exception {
	}

	public static class TestException1 extends Exception {
	}

	public static class TestException2 extends Exception {
	}

	public static class TestException3 extends Exception {
	}
}