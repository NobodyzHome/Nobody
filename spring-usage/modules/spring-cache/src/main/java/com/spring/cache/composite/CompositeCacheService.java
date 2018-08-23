package com.spring.cache.composite;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import com.spring.data.domain.BaseDept;

@Component

@CacheConfig(cacheNames = "dept_region")
@EnableCaching
public class CompositeCacheService {

	/*
	 * 共有三个地方可以配置缓存所使用的cacheManager,cacheResolver,keyGenerator等。分别是：
	 * 
	 * 1.spring配置文件中的cache:annotation-config(Globally configured)
	 * 
	 * 2.类上的@CacheConfig注解(class-level)
	 * 
	 * 3.方法上的@Cache*注解(operation-level)
	 * 
	 * 这三个的优先级是依次向上的，即当在这三个地方都设置了相同属性的配置，那么下面的会覆盖上面的属性的配置。
	 * 例如cache:annotation-config和@CacheConfig注解都给出了cacheManager的配置，那么会使用@
	 * CacheConfig配置的cacheManager。
	 * 如果@CacheConfig注解和@Cacheable注解都给出了cacheResolver的配置，那么该缓存操作会使用@
	 * Cacheable注解中的cacheResolver的配置。
	 */
	@Cacheable("date_region")
	public BaseDept cacheable1(Integer deptNo, String deptCode, String deptName) {
		BaseDept dept = new BaseDept(deptNo, deptCode);
		dept.setDeptName(deptName);

		return dept;
	}

	@Cacheable
	public BaseDept cacheable2(Integer deptNo, String deptCode, String deptName) {
		return cacheable1(deptNo, deptCode, deptName);
	}

	@Cacheable(cacheResolver = "specifiedCacheResolver")
	public BaseDept cacheable3(Integer deptNo, String deptCode, String deptName) {
		return cacheable1(deptNo, deptCode, deptName);
	}

	@Caching(cacheable = { @Cacheable, @Cacheable("date_region"),
			@Cacheable(cacheResolver = "specifiedCacheResolver") }, evict = { @CacheEvict(beforeInvocation = true),
					@CacheEvict(cacheNames = "date_region", beforeInvocation = true),
					@CacheEvict(cacheResolver = "specifiedCacheResolver", beforeInvocation = true) })
	public BaseDept multipleCacheable(Integer deptNo, String deptCode, String deptName) {
		return cacheable1(deptNo, deptCode, deptName);
	}

	@Caching(put = { @CachePut, @CachePut("date_region"), @CachePut(cacheResolver = "specifiedCacheResolver") })
	public BaseDept multiplePut(Integer deptNo, String deptCode, String deptName) {
		return cacheable1(deptNo, deptCode, deptName);
	}

	/*
	 * 该方法上的缓存操作的cacheName为no_region，并不是当前已知的region，
	 * 于是CompositeCacheManager就会使用NoOpCacheManager来解析这个cacheName，
	 * 并且必定会生成一个NoOpCache对象。该对象在进行get()方法时返回null，因此必定会导致连接点方法的调用
	 */
	@Cacheable("no_region")
	public BaseDept noOperCache(Integer deptNo, String deptCode, String deptName) {
		return cacheable1(deptNo, deptCode, deptName);
	}

	/*
	 * 注意：unless和condition的区别:
	 * 
	 * 1.unless属性可以存在于@Cacheable和@CachePut缓存操作中，但不能存在于@CacheEvict缓存操作。
	 * 因为unless是用于否决将调用目标方法的返回结果加入到Cache中，而@CacheEvict不是用于将value存储至Cache中。
	 * 但condition属性可以存在于@Cacheable、@CachePut、@CacheEvict缓存操作中。
	 * 
	 * 2.condition是在调用目标方法之前，就执行condition属性对应的spel表达式，
	 * 如果是true的话，才会进行缓存操作，否则不执行该缓存操作。而对于unless属性，是在调用目标方法之后，才执行unless属性的判断，
	 * 如果unless的spel表达式的计算结果为true的话，那么此次调用目标方法的返回结果不会被记录到Cache中。因此，
	 * 在unless的spel中，可以使用#result，来获得调用目标方法的返回结果。
	 * 
	 * 也就是说, condition是用于判断缓存操作是否应执行，而unless是用于否决将调用目标方法的返回结果添加到Cache中。
	 * 
	 * In addition the condition parameter, the unless parameter can be used to
	 * veto the adding of a value to the cache. Unlike condition, unless
	 * expressions are evaluated after the method has been called.
	 */
	@Cacheable(condition = "#deptNo<9999", unless = "#result.deptNo==1000")
	public BaseDept unlessCacheable(Integer deptNo, String deptCode, String deptName) {
		return cacheable1(deptNo, deptCode, deptName);
	}

	/*
	 * 在spel中可以使用#result，来获取目标方法的返回值的情况
	 * 
	 * 1.@Cacheable中的unless属性（因为unless是在执行目标方法后执行的）
	 * 
	 * 2.@CachePut中的condition,unless,key属性（因为CachePut是在调用连接点方法后执行的，
	 * 所有CachePut的所有spel的属性都可以使用#result，获取目标方法执行后的返回值）
	 * 
	 * 3.@CacheEvict(beforeInvocation=false)中的condition,key属性（原因和CachePut相同，
	 * 因为都是在调用目标方法后执行的）
	 * 
	 * 总结：
	 * 
	 * 1.对于所有缓存操作，在unless属性中肯定可以使用#result，因为unless属性是在调用目标方法后才会执行的。
	 * 
	 * 2.在调用目标方法后的缓存操作（@CachePut和@CacheEvict(beforeInvocation=false)），
	 * 其所有能使用spel的属性（例如key或condition属性），都可以使用#result来获取目标方法的返回结果。
	 */
	@CachePut(condition = "#result.deptNo>1000", unless = "#result.deptNo==2000", key = "#result.deptCode+'('+#result.deptName+')'")
	public BaseDept resultPut(Integer deptNo, String deptCode, String deptName) {
		return cacheable1(deptNo, deptCode, deptName);
	}

	// 在该缓存操作中，虽然调用目标方法的返回结果不会存储到Cache中，但目标方法的返回结果可以被应用于该缓存操作的condition和key属性中
	@CacheEvict(condition = "#result.deptNo>1000", key = "#result.deptCode+'('+#result.deptName+')'", beforeInvocation = false)
	public BaseDept resultEvict(Integer deptNo, String deptCode, String deptName) {
		return cacheable1(deptNo, deptCode, deptName);
	}

	@Cacheable(condition = "#deptNo>1000", unless = "#result.deptNo==2000", key = "#deptCode+'('+#deptName+')'")
	public BaseDept cacheable4(Integer deptNo, String deptCode, String deptName) {
		return cacheable1(deptNo, deptCode, deptName);
	}
}
