package com.spring.data.service;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.spring.data.dao.hibernate.BaseDeptHibernateDao;
import com.spring.data.dao.mapper.BaseDeptMapper;
import com.spring.data.domain.BaseDept;

@Service
@Transactional
@CacheConfig(cacheResolver = "allMapCacheResolver")
public class BaseDeptService {

	@Autowired
	private BaseDeptMapper deptMapper;

	@Autowired
	private BaseDeptHibernateDao deptHibernateDao;

	@Transactional(readOnly = true)
	@Caching(cacheable = { @Cacheable(key = "#dept.deptNo"),
			@Cacheable(cacheNames = "dept", cacheManager = "ehCacheManager", key = "#dept.deptNo") })
	public List<BaseDept> findByExample(BaseDept dept) {
		List<BaseDept> deptList = deptMapper.findByExample(dept);
		return deptList;
	}

	@Transactional(readOnly = true)
	@Caching(cacheable = { @Cacheable(key = "#dept.deptNo"),
			@Cacheable(cacheNames = "dept", cacheManager = "ehCacheManager", key = "#dept.deptNo") })
	@Async("threadPoolExecutor")
	public Future<List<BaseDept>> findByExampleAsync(BaseDept dept) {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ConcurrentUtils.constantFuture(findByExample(dept));
	}

	@Transactional
	@Cacheable(key = "#dept.deptNo")
	@Async("threadPoolTaskExecutor")
	public ListenableFuture<List<BaseDept>> findByExampleAsyncListenable(BaseDept dept) {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<BaseDept> deptList = findByExample(dept);
		AsyncResult<List<BaseDept>> futrue = new AsyncResult<List<BaseDept>>(deptList);

		return futrue;
	}

	@Transactional(readOnly = true)
	public List<BaseDept> findByExample1(Integer deptNo) {
		BaseDept dept = findById(deptNo);
		List<BaseDept> deptList = Collections.singletonList(dept);

		return deptList;
	}

	@Cacheable(cacheNames = "dept", cacheManager = "ehCacheManager", unless = "#result.deptNo >= 1000", key = "methodName+':'+#deptNo")
	@Transactional(readOnly = true)
	public BaseDept findById(Integer deptNo) {
		BaseDept dept = deptMapper.findById(deptNo);
		return dept;
	}

	@Async
	public Integer insertByExample(BaseDept example) {
		return deptMapper.insertByExample(example);
	}

	public Integer updateByParams(Map<String, Object> updateValues, Map<String, Object> conditions) {
		return deptMapper.updateByParams(updateValues, conditions);
	}

	public Integer deleteByParams(Integer[] deptNoArray, String[] deptCodeArray, Boolean isRun, BaseDept parentDept) {
		return deptMapper.deleteByParams(deptNoArray, deptCodeArray, isRun, parentDept);
	}

	private int test = 0;

	@Transactional(readOnly = true)
	@Cacheable(condition = "#startTime.after(#endTime)", sync = true, cacheNames = "line", cacheManager = "ehCacheManager")
	public Integer timeInterval(Date startTime, Date endTime) {
		/*
		 * 如果@Cacheable的sync属性为true，那么当有多个线程同时执行该方法，并传入相同的值时，目标方法只会被执行一次，
		 * 也就是只会输出一次test变量的值。而如果为false，那么多个线程同时执行该方法时，即使传入相同的值，
		 * 也有可能多个线程在为Cache放置计算结果前同时认为Cache中没有该key对应的value，于是会执行多次目标方法。
		 * 因此test变量的值就会被输出多次。
		 */
		System.out.println(++test);
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("startTime", startTime);
		paramMap.put("endTime", endTime);
		deptMapper.callFunction(paramMap);
		return (Integer) paramMap.get("intervalHours");
	}

	public Integer shiftModel(Integer lineNo, Date planDate) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("lineNo", lineNo);
		paramMap.put("planDate", planDate);

		deptMapper.callProcedure(paramMap);
		return (Integer) paramMap.get("shiftModel");
	}

	@Transactional(readOnly = true)
	@CachePut(key = "T(java.lang.Integer).valueOf(#result.deptCode)", condition = "#result.lines.size()>#deptNo")
	public BaseDept selectDeptWithLines(Integer deptNo) {
		BaseDept dept = deptMapper.selectDeptWithLines(deptNo);
		return dept;
	}

	@Transactional(readOnly = true)
	@Cacheable
	public BaseDept selectDeptWithLines1(Integer deptNo) {
		return selectDeptWithLines(deptNo);
	}

	@Transactional(readOnly = true)
	@Cacheable
	@Async("concurrentTaskExecutor")
	public Future<BaseDept> selectDeptWithLinesAsync(Integer deptNo) {
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BaseDept result = selectDeptWithLines(deptNo);
		Future<BaseDept> constantFuture = ConcurrentUtils.constantFuture(result);

		return constantFuture;
	}

	@Transactional(readOnly = true)
	@Caching(cacheable = @Cacheable, evict = { @CacheEvict(beforeInvocation = true, condition = "#deptNo<=2"),
			@CacheEvict(beforeInvocation = false, condition = "#result.lines.size()>#deptNo") })
	public BaseDept deptWithAllInfo(Integer deptNo) {
		BaseDept dept = deptMapper.deptWithAllInfo(deptNo);
		return dept;
	}

	@Transactional(readOnly = true)
	@Caching(cacheable = @Cacheable, evict = { @CacheEvict(beforeInvocation = true, condition = "#deptNo<=2"),
			@CacheEvict(beforeInvocation = false, condition = "#result instanceof T(org.springframework.scheduling.annotation.AsyncResult)") })
	@Async
	public ListenableFuture<BaseDept> deptWithAllInfo1(Integer deptNo) {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		BaseDept dept = deptMapper.deptWithAllInfo(deptNo);
		AsyncResult<BaseDept> result = new AsyncResult<>(dept);

		return result;
	}

	@Transactional(readOnly = true, transactionManager = "hibernateTransactionManager")
	public BaseDept findByIdHibernate(Integer deptNo) {
		BaseDept dept = deptHibernateDao.findById(deptNo);
		return dept;
	}

	@Transactional(readOnly = true, transactionManager = "hibernateTransactionManager")
	@Async("threadPoolTaskExecutor")
	public ListenableFuture<BaseDept> findByIdHibernateAsync(Integer deptNo) {
		BaseDept dept = findByIdHibernate(deptNo);
		AsyncResult<BaseDept> asyncResult = new AsyncResult<BaseDept>(dept);
		return asyncResult;
	}

	public Integer saveOrUpdate(BaseDept dept) {
		if (Objects.nonNull(dept.getDeptNo()) && Objects.nonNull(findById(dept.getDeptNo()))) {
			Map<String, Object> map = new HashMap<>();
			map.put("dept_code", dept.getDeptCode());
			map.put("dept_name", dept.getDeptName());

			Map<String, Object> condition = new HashMap<>();
			condition.put("dept_no", dept.getDeptNo());

			return updateByParams(map, condition);
		} else {
			return insertByExample(dept);
		}
	}

	@Scheduled(initialDelay = 5000, fixedDelay = 10000)
	@Caching(evict = { @CacheEvict(cacheResolver = "allEhcacheResolver", allEntries = true),
			@CacheEvict(allEntries = true) })
	public void clearCache() {
		System.out.println("BaseDeptService.clearCache()");
	}

}