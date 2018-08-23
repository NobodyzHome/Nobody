package com.spring.data.service;

import java.util.List;
import java.util.concurrent.Future;

import javax.validation.constraints.Min;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.spring.data.dao.BaseDeptMapper;
import com.spring.data.domain.BaseDept;

@Service
@Profile("deptService")
@CacheConfig(cacheNames = { "hello", "world" }, cacheManager = "concurrentCacheManager")
@Validated
public class BaseDeptService {

	@Autowired
	private BaseDeptMapper deptMapper;

	@Transactional
	@Caching(cacheable = {
			@Cacheable(condition = "T(org.springframework.util.StringUtils).hasText(#deptName)", unless = "#result.size() <= 0"),
			@Cacheable(cacheResolver = "ehCacheResolver") }, evict = {
					@CacheEvict(allEntries = true, beforeInvocation = true, condition = "#deptName.equals('总公司')"),
					@CacheEvict(beforeInvocation = false, cacheResolver = "ehCacheResolver", condition = "#result.size() <= 0", key = "#deptName") })
	public List<BaseDept> queryDepts(String deptName) {
		List<BaseDept> deptList = deptMapper.queryDepts(deptName);
		Object proxy = AopContext.currentProxy();
		if (BaseDeptService.class.isInstance(proxy)) {
			BaseDeptService proxyDeptService = BaseDeptService.class.cast(proxy);
			deptList.forEach(dept -> {
				int deptNo = dept.getDeptNo();
				BaseDept queriedDept = proxyDeptService.queryDept(deptNo);
				System.out.println(queriedDept.getDeptName());
			});
		}

		return deptList;
	}

	@Cacheable(condition = "#deptNo<=8000", unless = "T(java.util.Objects).isNull(#result)")
	@Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true, propagation = Propagation.REQUIRES_NEW)
	public BaseDept queryDept(int deptNo) {
		BaseDept dept = deptMapper.queryDept(deptNo);
		return dept;
	}

	// @Scheduled(fixedRateString = "#{${clearCacheInterval}*1000+${offset}}")
	@Caching(evict = { @CacheEvict(allEntries = true),
			@CacheEvict(allEntries = true, cacheResolver = "ehCacheResolver") })
	public void clearCache() {
	}

	@Transactional
	@Cacheable(cacheResolver = "ehCacheResolver", condition = "#deptNo<=8888")
	@Async
	public Future<BaseDept> queryDeptAsync(@Min(value = 0, message = "{Min}") Integer deptNo) {
		BaseDept dept = queryDept(deptNo);
		return ConcurrentUtils.constantFuture(dept);
	}

	@Async
	public void testAsyncException(@Min(value = 0, message = "{Min}") Integer deptNo) {
		System.out.println(deptNo);
	}
}
