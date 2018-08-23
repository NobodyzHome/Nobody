package com.spring.data.service;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.spring.data.dao.mybatis.BaseDeptMapper;
import com.spring.data.domain.BaseDept;

@Service
/*
 * 使用@EnableCaching启动对缓存的支持，那么是按类型获取默认的cacheManager。此时如果有多个CacheManager，
 * 想用哪个CacheManager，就在那个CacheManager前加@Primary即可
 * 
 * 在类的方法上增加@Cacheable、@Transactional注解，在将这个类注册到容器后，容器可以对这些方法进行代理。
 * 但仅仅是将这些类注册到容器的<bean>中是不够的，还需要让容器能识别这些注解，然后在注解的方法上生成代理。
 * 
 * 此时有两种方法能让容器识别<bean>的方法上的这些注解。一种是在注解的类上增加@EnableX的注解，另一种是在配置文件中给出cache:
 * annotation-config等类似元素。但注意，此时都需要使用component-scan来将类注册到容器中，而不是简单的使用<bean>
 * 来向容器注册这个类。
 */
@EnableCaching
@CacheConfig(cacheNames = { "date_region", "dept_region", "hello", "test" })
@Validated
public class BaseDeptService {

	@Autowired
	private BaseDeptMapper deptMapper;

	@Cacheable(key = "#condition.deptNo", condition = "#condition.deptNo<9999", unless = "T(org.apache.commons.lang3.ArrayUtils).isEmpty(#result)")
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	// 注意：Validator只会检验有值的属性。例如deptNo属性设置了@Length注解，但deptNo属性没有设置值，那么Validator不会对这个属性进行验证。
	public BaseDept[] queryDept(@Valid BaseDept condition) {
		System.out.println("BaseDeptService.queryDept()");
		BaseDept[] deptsSearched = deptMapper.queryDept(condition);

		return deptsSearched;
	}

	@Transactional
	public int insertDept(BaseDept deptToInsert) {
		System.out.println("BaseDeptService.insertDept()");
		int deptNo = deptMapper.queryDeptId();
		deptToInsert.setDeptNo(deptNo);

		int affectedRows = deptMapper.insertDept(deptToInsert);
		return affectedRows;
	}

	@CacheEvict(beforeInvocation = false, allEntries = true, condition = "#result >= 1")
	@Transactional
	public int updateDept(BaseDept deptToUpdate, BaseDept condition) {
		int affectedRows = deptMapper.updateDept(deptToUpdate, condition);
		return affectedRows;
	}

	@CacheEvict(beforeInvocation = false, allEntries = true, condition = "#result >= 1")
	@Transactional
	public int deleteDept(BaseDept deptToDelete) {
		int affectedRows = deptMapper.deleteDept(deptToDelete);
		return affectedRows;
	}
}
