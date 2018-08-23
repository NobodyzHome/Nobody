package com.spring.web.controller;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.data.domain.BaseDept;
import com.spring.data.service.BaseDeptService;

@Controller
@RequestMapping("/dept")
public class BaseDeptController {

	@Autowired
	private BaseDeptService deptService;

	@ResponseBody
	@RequestMapping("/queryList")
	public List<BaseDept> queryDeptList(BaseDept condition) {
		List<BaseDept> deptList = deptService.queryDeptList(condition);
		return deptList;
	}

	@ResponseBody
	@RequestMapping("/timeInterval/{methodName}/{startTime}/{endTime}")
	public String timeInterval(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss") Date startTime,
			@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss") Date endTime,
			@PathVariable String methodName) {
		Method timestampChaMethod = ReflectionUtils.findMethod(deptService.getClass(), methodName, Date.class,
				Date.class);
		ReflectionUtils.makeAccessible(timestampChaMethod);
		Object interval = ReflectionUtils.invokeMethod(timestampChaMethod, deptService, startTime, endTime);

		return interval.toString();
	}

	@ResponseBody
	@RequestMapping("/asyncQueryList")
	public List<BaseDept> queryDeptListAsync(BaseDept condition) throws InterruptedException, ExecutionException {
		Future<List<BaseDept>> futureResult = deptService.queryDeptListAsync(condition);
		List<BaseDept> deptList = futureResult.get();
		return deptList;
	}
}