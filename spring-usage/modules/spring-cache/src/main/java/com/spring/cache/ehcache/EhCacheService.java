package com.spring.cache.ehcache;

import java.util.Date;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import com.spring.data.domain.BaseDept;

@EnableCaching
@Component
@CacheConfig(cacheNames = "dept_region")
public class EhCacheService {

	// 如果没有给出cacheManager的话，则使用cache:annotation-config中给出的默认的cache-manager
	@Cacheable
	public BaseDept cacheableQuery1(BaseDept condition, Date modifyDate) {
		BaseDept deptNew = new BaseDept();
		deptNew.setDeptNo(condition.getDeptNo());
		deptNew.setDeptCode(condition.getDeptCode());
		deptNew.setDeptName(condition.getDeptName());
		deptNew.setIsRun(condition.getIsRun());
		deptNew.setModifyDate(modifyDate);

		return deptNew;
	}

	@Cacheable(cacheResolver = "specifiedCacheResolver")
	public BaseDept cacheableQuery2(BaseDept condition, Date modifyDate) {
		return cacheableQuery1(condition, modifyDate);
	}

	@Cacheable("date_region")
	public BaseDept cacheableQuery3(BaseDept condition, Date modifyDate) {
		return cacheableQuery1(condition, modifyDate);
	}

	@Caching(cacheable = { @Cacheable, @Cacheable(cacheResolver = "specifiedCacheResolver"),
			@Cacheable("date_region") }, evict = @CacheEvict(cacheNames = { "dept_region", "date_region",
					"line_region" }, beforeInvocation = true))
	public BaseDept multipleCacheableQuery(BaseDept condition, Date modifyDate) {
		return cacheableQuery1(condition, modifyDate);
	}

	@Caching(put = { @CachePut, @CachePut("date_region"), @CachePut(cacheResolver = "specifiedCacheResolver") })
	public BaseDept multipulePutQuery(BaseDept condition, Date modifyDate) {
		return cacheableQuery1(condition, modifyDate);
	}
	
}