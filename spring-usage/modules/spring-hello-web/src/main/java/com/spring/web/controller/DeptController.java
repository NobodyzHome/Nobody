package com.spring.web.controller;

import java.io.PrintWriter;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.spring.data.domain.BaseDept;
import com.spring.data.service.BaseDeptService;

@Controller
@RequestMapping("/dept")
@SessionAttributes(types = BaseDept.class)
public class DeptController {

	@Autowired
	private BaseDeptService deptService;

	@RequestMapping("/queryById/{deptNo}")
	@ResponseBody
	public BaseDept queryDeptDetail(@PathVariable @NumberFormat(pattern = "#公司") Integer deptNo) {
		BaseDept dept = deptService.deptWithAllInfo(deptNo);

		return dept;
	}

	@RequestMapping(path = "/queryById/hibernate", params = "deptNo")
	@ResponseBody
	public BaseDept queryDeptHibernate(@NumberFormat(pattern = "co,#") Integer deptNo) {
		BaseDept dept = deptService.findByIdHibernate(deptNo);

		return dept;
	}

	@RequestMapping("/timeInterval/{startTime}/{endTime}")
	public void queryTimeInterval(PrintWriter writer,
			@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss") Date startTime,
			@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss") Date endTime) {
		Integer interval = deptService.timeInterval(startTime, endTime);
		writer.println(interval);
	}

	@RequestMapping("/show/{deptNo}")
	public String showDept(@PathVariable Integer deptNo, Model model) {
		BaseDept dept = deptService.findById(deptNo);
		model.addAttribute(dept);

		return "show/dept";
	}

	@PostMapping("/saveOrUpdate")
	public String saveOrUpdateDept(BaseDept dept) {
		Integer result = deptService.saveOrUpdate(dept);
		System.out.println(result);

		return "show/dept";
	}

	@GetMapping("/delete")
	public String deleteDept(BaseDept dept) {
		Integer[] deptNoArray = { dept.getDeptNo() };

		int result = deptService.deleteByParams(deptNoArray, null, null, null);
		System.out.println(result);

		return "show/dept";
	}
}