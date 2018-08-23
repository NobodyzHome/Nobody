package com.spring.web.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.spring.data.domain.BaseDept;

@Controller
@RequestMapping("/test")
@CacheConfig(cacheNames = "controller")
public class TestController {

	@Autowired
	private ResourceLoader resourceLoader;

	@ResponseBody
	@GetMapping("/hello/{words}")
	@Caching(evict = { @CacheEvict(allEntries = true),
			@CacheEvict(cacheResolver = "cacheResolver", allEntries = true) })
	public String helloWorld(@PathVariable String words) {
		return words;
	}

	@RequestMapping("/model")
	public String testModel(@ModelAttribute("hello") String hello) {
		return hello;
	}

	@ResponseBody
	@GetMapping("/exception/{divisor}/{dividend}")
	@Cacheable(condition = "#dividend!=0", unless = "T(java.lang.Integer).valueOf(#result) > 9999")
	public String testException(@PathVariable int divisor, @PathVariable int dividend) {
		int result = divisor / dividend;
		return String.valueOf(result);
	}
	
	@Cacheable(cacheResolver = "cacheResolver", key = "#dept.deptNo", condition = "T(java.util.Objects).nonNull(#dept.deptNo)")
	@RequestMapping(path = "/validation", params = { "deptNo", "deptName", "modifyDate" })
	public String testValidation(@Valid BaseDept dept, BindingResult validationResult) {
		return "dept";
	}

	/*
	 * 注意：如果想将请求参数dept的值，使用ConversionService转换成BaseDept类型的对象，
	 * 那么必须要在请求处理方法的BaseDept类型的参数上增加@RequestParam注解，否则spring会将该参数视作@
	 * ModelAttribute来处理
	 */
	@CachePut(cacheResolver = "cacheResolver", condition = "#result.equals('dept')", key = "#dept.deptNo", unless = "T(java.util.Objects).isNull(#dept.deptNo)")
	@GetMapping(path = "/conversion", params = "dept")
	public String testConversion(@RequestParam BaseDept dept, Model model) {
		model.addAttribute(dept);
		return "dept";
	}

	@ResponseBody
	@GetMapping("/download")
	public Resource testDownload(@RequestParam String filePath, HttpServletResponse response)
			throws FileNotFoundException {
		String fileName = StringUtils.getFilename(filePath);
		response.setContentType("attachment; fileName=" + fileName);

		String realPath = "/upload/" + filePath;
		Resource resource = resourceLoader.getResource(realPath);

		if (resource.exists()) {
			return resource;
		} else {
			throw new FileNotFoundException("在指定路径【" + realPath + "】中没有找到文件【" + fileName + "】");
		}
	}

	@PostMapping("/upload")
	@ResponseBody
	public BaseDept[] testUpload(@RequestParam MultipartFile file1, @RequestPart("file1") BaseDept dept1,
			@RequestParam Part file2, @RequestPart("file2") BaseDept dept2) throws IllegalStateException, IOException {
		Resource file1Resource = resourceLoader.getResource("/upload/" + file1.getOriginalFilename());
		Resource file2Resource = resourceLoader.getResource("/upload/" + file2.getSubmittedFileName());

		FileUtils.writeByteArrayToFile(file1Resource.getFile(), file1.getBytes());
		FileUtils.copyInputStreamToFile(file2.getInputStream(), file2Resource.getFile());

		return new BaseDept[] { dept1, dept2 };
	}

	@RequestMapping("/uploadThenShow")
	public String uploadThenShow(MultipartFile file1, MultipartFile file2, HttpServletRequest request, Model model)
			throws IOException {
		ServletContext servletContext = request.getServletContext();
		String servletContextPath = servletContext.getRealPath("/");
		File servletContextFolder = new File(servletContextPath);

		File uploadFile1 = new File(servletContextFolder, "WEB-INF/images/" + file1.getOriginalFilename());
		File uploadFile2 = new File(servletContextFolder, "WEB-INF/images/" + file2.getOriginalFilename());

		FileUtils.writeByteArrayToFile(uploadFile1, file1.getBytes());
		FileUtils.writeByteArrayToFile(uploadFile2, file2.getBytes());

		String contextPath = servletContext.getContextPath();
		model.addAttribute("img1", contextPath + "/images/" + file1.getOriginalFilename());
		model.addAttribute("img2", contextPath + "/images/" + file2.getOriginalFilename());

		return "uploadThenShow";
	}
}
