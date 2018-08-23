package com.spring.data.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.data.domain.BaseDept;
import com.spring.data.mapper.BaseDeptMapper;

@Service
public class BaseDeptService {

	@Autowired
	private BaseDeptMapper deptMapper;

	@Transactional
	public List<BaseDept> queryDeptList(BaseDept condition) {
		List<BaseDept> deptList = deptMapper.queryDept(condition);
		return deptList;
	}

	@Transactional
	public Integer timestampCha(Date startTime, Date endTime) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("startTime", startTime);
		paramMap.put("endTime", endTime);

		deptMapper.timeCha(paramMap);

		Integer interval = (Integer) paramMap.get("interval");

		return interval;
	}

	@Transactional
	public Integer timestampCha1(Date startTime, Date endTime) {
		Integer interval = deptMapper.timeCha1(startTime, endTime);
		return interval;
	}

	@Async
	@Transactional
	public Future<List<BaseDept>> queryDeptListAsync(BaseDept condition) {
		List<BaseDept> deptList = deptMapper.queryDept(condition);
		Future<List<BaseDept>> result = ConcurrentUtils.constantFuture(deptList);
		return result;
	}
}
