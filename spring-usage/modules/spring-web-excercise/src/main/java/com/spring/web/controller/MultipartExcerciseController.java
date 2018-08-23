package com.spring.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
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

@Controller
@RequestMapping("/multipart/excercise")
@SessionAttributes({ "view", "action" })
public class MultipartExcerciseController {

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private Environment environment;

	@ModelAttribute("urlPrefix")
	public String urlPrefix(@Value("${urlPrefix}") String urlPrefix) {
		return urlPrefix;
	}

	@GetMapping(path = "/show", params = { "view", "action" })
	public String showPage(String view, String action, Model model) {
		model.addAttribute("view", view).addAttribute("action", action);

		return view;
	}

	@PostMapping(path = "/upload")
	public String testUpload(@ModelAttribute("view") String view, @RequestPart("fileA") byte[] file1, Part fileB,
			@RequestPart MultipartFile fileC, @Value("classpath:") Resource classpath) throws IOException {
		File classpathFolder = classpath.getFile();

		FileUtils.writeByteArrayToFile(new File(classpathFolder, "unnamed.txt"), file1);
		FileUtils.copyInputStreamToFile(fileB.getInputStream(),
				new File(classpathFolder, fileB.getSubmittedFileName()));
		FileUtils.writeByteArrayToFile(new File(classpathFolder, fileC.getOriginalFilename()), fileC.getBytes());

		return view;
	}

	@GetMapping("/download/{fileName}")
	@ResponseBody
	public Resource testDownload(@PathVariable String fileName, @MatrixVariable String folder,
			HttpServletResponse response) {
		String pathToDownload = "WEB-INF/" + StringUtils.replace(folder, ".", "/") + "/" + fileName;
		Resource resourceToDownload = resourceLoader.getResource(pathToDownload);

		response.setHeader("content-Disposition", "attachment;filename=" + fileName);

		return resourceToDownload;
	}

	@PostMapping("/uploadThenDownload")
	@ResponseBody
	public byte[] testUploadThenDownload(@RequestPart byte[] fileA, Part fileB,
			@RequestParam("fileC") MultipartFile thirdPart, HttpServletResponse response) throws IOException {
		response.setHeader("content-Disposition", "attachment;filename=merged.txt");

		byte[] result = ArrayUtils.addAll(fileA, StreamUtils.copyToByteArray(fileB.getInputStream()));
		result = ArrayUtils.addAll(result, thirdPart.getBytes());

		return result;
	}

	@PostMapping("/uploadThenShow")
	public String testUploadThenShow(MultipartFile fileA, MultipartFile fileB, MultipartFile fileC,
			HttpServletRequest request, Model model, @ModelAttribute("view") String view) {
		MultipartFile[] files = { fileA, fileB, fileC };
		AtomicInteger index = new AtomicInteger(0);
		String contextPath = request.getServletContext().getRealPath("");

		Arrays.stream(files)
				.filter(file -> MediaType.valueOf("image/*").isCompatibleWith(MediaType.valueOf(file.getContentType())))
				.forEach(file -> {
					String filePath = contextPath + "/WEB-INF/images/" + file.getOriginalFilename();
					File imageToUpload = new File(filePath);

					try {
						FileUtils.writeByteArrayToFile(imageToUpload, file.getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}

					model.addAttribute("img" + index.addAndGet(1), environment
							.resolveRequiredPlaceholders("${urlPrefix}/images/" + file.getOriginalFilename()));
				});

		return view;
	}
}
