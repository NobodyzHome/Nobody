package com.spring.redis.data.service;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import com.spring.redis.data.domain.BaseDept;
import com.spring.redis.data.domain.BaseLine;
import com.spring.redis.data.domain.DomainUtils;

@Service
@CacheConfig(cacheNames = "hello")
public class CacheService {

	@Cacheable(cacheNames = { "hello", "world" })
	public String test1(Integer a, Integer b) {
		return String.valueOf(a * b);
	}

	// @Scheduled(fixedDelay = 5000, initialDelay = 10000,)
	@CacheEvict(allEntries = true, beforeInvocation = false, cacheNames = { "hello", "world" })
	public void clearCache() {
		System.out.println("清理缓存完毕");
	}

	@Async("threadPoolTaskExecutor")
	public ListenableFuture<Date> test2(Date date1, Integer offset) {
		Date date = DateUtils.addDays(date1, -offset);

		return new AsyncResult<>(date);
	}

	@Cacheable(cacheNames = "dept", cacheManager = "cacheManager2")
	public Date test3(Date date, Integer offset) {
		date = DateUtils.addDays(date, -offset);
		return date;
	}

	@Caching(cacheable = { @Cacheable, @Cacheable(cacheNames = { "dept", "user" }, cacheManager = "cacheManager2") })
	public BaseDept test4(int deptNo) {
		BaseDept dept = DomainUtils.generateDept(deptNo);

		return dept;
	}

	@Cacheable(cacheResolver = "cacheResolver")
	public BaseLine test5(int lineNo, int deptNo) {
		BaseLine line = DomainUtils.generateLine(lineNo, deptNo);

		return line;
	}

	@Cacheable(cacheManager = "cacheManager4", cacheNames = "tx")
	public BaseLine test6(int lineNo, int deptNo) {
		return test5(lineNo, deptNo);
	}

	@Cacheable(cacheManager = "cacheManager5", cacheNames = { "line", "hello" })
	public BaseLine test7(Date modifyDate) {
		BaseLine line = DomainUtils.generateLine();
		line.setModifyDate(modifyDate);

		return line;
	}

	@Cacheable(cacheManager = "cacheManager5", cacheNames = { "world", "test", "user" })
	public BaseDept test8(int deptNo, String deptCode, String deptName) {
		BaseDept dept = new BaseDept(deptNo, deptCode, deptName);

		return dept;
	}
}
