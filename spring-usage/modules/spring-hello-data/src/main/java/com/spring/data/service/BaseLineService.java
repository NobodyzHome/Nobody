package com.spring.data.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;

import com.spring.data.dao.hibernate.BaseLineHibernateDao;
import com.spring.data.dao.mapper.BaseLineMapper;
import com.spring.data.domain.BaseLine;

@Service
public class BaseLineService {

	@Autowired
	private BaseLineHibernateDao lineDao;

	@Autowired
	private BaseLineMapper lineMapper;

	@Value("No:1111,No:2222,No:3333")
	@NumberFormat(pattern = "No:#")
	private Integer[] lineNoArray;

	@Transactional("hibernateTransactionManager")
	public BaseLine queryByIdHibernate(Integer lineNo) {
		BaseLine line = lineDao.queryById(lineNo);

		return line;
	}

	@Transactional
	public BaseLine queryById(Integer lineNo) {
		BaseLine line = lineMapper.selectLineWithDept(lineNo);
		return line;
	}

	@Transactional
	@Async
	public ListenableFuture<BaseLine> queryByIdAsync(Integer lineNo) {
		BaseLine line = queryById(lineNo);
		AsyncResult<BaseLine> asyncResult = new AsyncResult<>(line);

		return asyncResult;
	}

	@Transactional
	@Async("simpleAsyncTaskExecutor")
	public void saveOrUpdate(BaseLine example) {
		if (Objects.nonNull(example.getLineNo()) && Objects.nonNull(queryById(example.getLineNo()))) {
			Map<String, Object> condition = new HashMap<>();
			condition.put("line_no", example.getLineNo());

			lineMapper.updateByExample(example, condition);
		} else {
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("line_no", example.getLineNo());
			paramMap.put("line_code", example.getLineCode());
			paramMap.put("line_name", example.getLineName());
			paramMap.put("is_run", example.getIsRun());
			paramMap.put("modify_time", example.getModifyDate());

			lineMapper.insertByMap(paramMap);
		}
	}

	@Transactional("hibernateTransactionManager")
	public void delete(Integer lineNo) {
		lineDao.deleteById(lineNo);
	}

	@Scheduled(fixedDelay = 5000)
	public void showLines() {
		List<BaseLine> lineList = lineMapper.selectById(lineNoArray);
		lineList.forEach(line -> System.out.println(line.getLineName()));
	}

	@Async("threadPoolTaskExecutor")
	public void testException1(boolean throwEx) throws SQLException {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (throwEx) {
			throw new SQLException("执行sql语句发生错误");
		}
	}

	@Async
	public ListenableFuture<Integer> testException2(Integer value) throws Exception {
		Thread.sleep(3000);
		if (value < 0) {
			throw new Exception("async exception");
		}

		return new AsyncResult<>(value);
	}

	@Async
	public Future<Integer> testException3(Integer value) throws Exception {
		Thread.sleep(3000);
		if (value < 0) {
			throw new Exception("async exception");
		}

		return new AsyncResult<>(value);
	}

	@Async("simpleAsyncTaskExecutor")
	public String testException4(String words) throws Exception {
		if (StringUtils.hasText(words)) {
			return words;
		}
		throw new Exception("async exception test");
	}
}