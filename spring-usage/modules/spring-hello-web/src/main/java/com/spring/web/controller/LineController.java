package com.spring.web.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.spring.data.domain.BaseLine;
import com.spring.data.service.BaseLineService;

@Controller
@RequestMapping("/line")
@SessionAttributes(types = BaseLine.class)
public class LineController {

	@Autowired
	private BaseLineService lineService;

	@RequestMapping("/hibernate/{condition}/show")
	@ResponseBody
	public BaseLine queryByIdHibernate(@MatrixVariable Integer lineNo) {
		BaseLine line = lineService.queryByIdHibernate(lineNo);
		return line;
	}

	@RequestMapping(path = "/mybatis", params = "lineNo")
	@ResponseBody
	public BaseLine queryById(Integer lineNo) {
		BaseLine line = lineService.queryById(lineNo);
		return line;
	}

	@GetMapping("/show/{lineNo}")
	public String showLine(@PathVariable Integer lineNo, Model model) {
		BaseLine line = Optional.ofNullable(lineService.queryByIdHibernate(lineNo)).orElseGet(BaseLine::new);
		model.addAttribute(line);

		return "show/line";
	}

	@PostMapping("/saveOrUpdate")
	public String saveOrUpdate(BaseLine line) {
		lineService.saveOrUpdate(line);

		return "show/line";
	}

	@GetMapping("/delete/{lineNo}")
	public String delete(@PathVariable Integer lineNo) {
		lineService.delete(lineNo);

		return "show/line";
	}
}