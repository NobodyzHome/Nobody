package com.spring.web.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.WebUtils;

import com.spring.data.domain.BaseDept;
import com.spring.data.domain.BaseLine;
import com.spring.web.converter.BaseDeptFormatter;

@Controller
@RequestMapping("/multipart")
@SessionAttributes({ "type", "view" })
public class TestMultipartController {

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private Environment environment;

	@ModelAttribute("prefix")
	public String dispatcherPrefix(@Value("${urlPrefix}") String prefix) {
		return prefix;
	}

	@RequestMapping("/show/{view}")
	public String show(@PathVariable String view, Type type, Model model) {
		model.addAttribute("view", view).addAttribute(type);
		return view;
	}

	// 在获取MultipartFile或Part类型参数时，写不写@RequestParam注解都是可以的
	@PostMapping("/requestParam")
	public String testRequestParam(@ModelAttribute("view") String view, @RequestParam MultipartFile file1, Part file2,
			MultipartFile file3, @Value("classpath:") Resource classpath) throws IOException {
		File file1Path = new File(classpath.getFile(), file1.getOriginalFilename());
		File file3Path = new File(classpath.getFile(), file3.getOriginalFilename());

		file1.transferTo(file1Path);
		/*
		 * Part类的write方法是将捕获的内容写成web.xml的<multipart-config>
		 * 的location属性值对应的目录下的指定名字的文件
		 * 
		 * 也就是说<multipart-config>的location属性值决定了Part类的write方法将文件写到哪个目录下
		 */
		file2.write(file2.getSubmittedFileName());
		file3.transferTo(file3Path);

		return view;
	}

	/*
	 * @RequestPart是使用HttpMessageConverter来进行参数值的获取。根据要处理的多部分请求中的某一部分的Content-
	 * Type来确定媒体类型。
	 * 
	 * 注意：一提到HttpMessageConverter就需要想到要转换的参数类型以及媒体类型，
	 * 根据这两个内容来确定使用哪个HttpMessageConverter具体类来进行处理。
	 */
	@PostMapping("/requestPart")
	public String testRequestPart(@ModelAttribute("view") String view, @RequestPart("file1") BaseDept dept,
			@RequestPart("file2") BaseLine line, @RequestPart String file3) {
		return view;
	}

	@PostMapping("/requestParamAndPart")
	public String testRequestParamAndPart(@ModelAttribute("view") String view, @RequestPart("file1") BaseDept dept,
			@RequestPart("file2") BaseLine line, @RequestPart("file3") BaseDept deptFormatted, Part file3,
			@RequestParam("file1") MultipartFile multipartFile) throws IOException {
		String file3Contents = StreamUtils.copyToString(file3.getInputStream(), Charset.defaultCharset());
		String file1Contents = new String(multipartFile.getBytes());

		System.out.println(file3Contents);
		System.out.println(file1Contents);

		return view;
	}

	/*
	 * 文件下载的整体思路就是两点：
	 * 
	 * 1.获取要下载的文件的内容，并将内容输出到目的端
	 * 
	 * a) 使用ResourceLoader获取文件内容，并将获取的Resource对象作为请求处理方法的返回值
	 * 
	 * b) 在请求处理方法上增加@ResponseBody注解，用于让HttpMessageConverter输出返回的Resource对象
	 * 
	 * 2.设置响应头，增加Content-Disposition属性，使客户端浏览器看见该响应头后，就知道不要将响应内容输出到页面，而是弹出一个下载框，
	 * 供用户下载
	 */
	@GetMapping("/download/{file}/test")
	@ResponseBody
	public Resource testDownload(@PathVariable("file") String fileName, @MatrixVariable String folder,
			HttpServletResponse response) {
		response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

		String realFile = "WEB-INF/" + StringUtils.replace(folder, ".", "/") + "/" + fileName;
		Resource resource = resourceLoader.getResource(realFile);
		return resource;
	}

	@PostMapping("/uploadThenDown")
	@ResponseBody
	public byte[] testUploadThenDown(@RequestPart byte[] file1, Part file2,
			@RequestParam("file3") MultipartFile thirdFile, HttpServletResponse response) throws IOException {
		response.setHeader("Content-Disposition", "attachment;filename=merged.txt");

		byte[] file2Bytes = StreamUtils.copyToByteArray(file2.getInputStream());
		byte[] file3Bytes = thirdFile.getBytes();

		byte[] result = ArrayUtils.addAll(file1, file2Bytes);
		result = ArrayUtils.addAll(result, file3Bytes);

		return result;
	}

	@PostMapping("/uploadThenShow")
	public String testUploadThenShow(Part file1, Part file2, Part file3, @Value("classpath:") Resource classpath,
			Model model, @ModelAttribute("view") String view, HttpServletRequest request) {

		Part[] parts = { file1, file2, file3 };
		AtomicInteger index = new AtomicInteger(0);

		Arrays.stream(parts)
				.filter(part -> MediaType.valueOf("image/*").isCompatibleWith(MediaType.valueOf(part.getContentType())))
				.forEach(part -> {
					try {
						String servletContextPath = WebUtils.getRealPath(request.getServletContext(), "");
						File file = new File(servletContextPath, "WEB-INF/images/" + part.getSubmittedFileName());

						FileUtils.copyToFile(part.getInputStream(), file);
						model.addAttribute("img" + index.addAndGet(1), environment
								.resolveRequiredPlaceholders("${urlPrefix}/images/" + part.getSubmittedFileName()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				});

		return view;
	}

	public enum Type {

		PARAM("requestParam"), PART("requestPart"), BOTH("requestParamAndPart"), UP_DOWN("uploadThenDown"), UP_SHOW(
				"uploadThenShow");

		private String words;

		private Type(String words) {
			this.words = words;
		}

		public String getWords() {
			return words;
		}

	}

	@InitBinder
	public void initDataBinder(DataBinder binder) {
		ConversionService conversionService = binder.getConversionService();
		FormattingConversionService configConversionService = (FormattingConversionService) conversionService;
		configConversionService.addFormatter(new BaseDeptFormatter());
	}
}
