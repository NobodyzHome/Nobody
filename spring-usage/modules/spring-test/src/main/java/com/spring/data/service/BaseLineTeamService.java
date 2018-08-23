package com.spring.data.service;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.data.dao.BaseLineTeamMapper;
import com.spring.data.domain.BaseLineTeam;

@Service
@CacheConfig(cacheResolver = "ehCacheResolver")
public class BaseLineTeamService {

	@Autowired
	private BaseLineTeamMapper lineTeamMapper;

	@Transactional
	@Cacheable
	public List<BaseLineTeam> queryAll() {
		List<BaseLineTeam> lineTeamList = lineTeamMapper.queryAll();
		System.out.println("BaseLineTeamService.queryAll()");
		return lineTeamList;
	}

	@Async("syncTaskExecutor")
	@Transactional
	@Cacheable
	public Future<List<BaseLineTeam>> queryAllSync() {
		List<BaseLineTeam> lineTeamList = queryAll();
		Future<List<BaseLineTeam>> lineTeamResult = ConcurrentUtils.constantFuture(lineTeamList);
		return lineTeamResult;
	}

	@Async("simpleAsyncTaskExecutor")
	@Transactional
	@Cacheable
	public Future<List<BaseLineTeam>> queryAllAsync() {
		List<BaseLineTeam> lineTeamList = queryAll();
		Future<List<BaseLineTeam>> lineTeamResult = ConcurrentUtils.constantFuture(lineTeamList);
		return lineTeamResult;
	}

	// fixedDelayString属性用于给出字符串形式的毫秒数，主要用于给出占位符。
	@Scheduled(fixedDelayString = "${fixedDelay}")
	public void queryAllInterval() {
		BaseLineTeamService proxyService = (BaseLineTeamService) AopContext.currentProxy();
		List<BaseLineTeam> lineTeamList = proxyService.queryAll();
		System.out.println("BaseLineTeamService.queryAllInterval()");
		lineTeamList.forEach(lineTeam -> System.out.println(lineTeam.getGroupName() + ":" + lineTeam.getDescription()));
	}

	// 可以为一个方法设置多个@Scheduled，当然这是基于java8，实际上是把这多个@Scheduled创建成一个@Schedules对象
	@Scheduled(fixedRate = 3000)
	@Scheduled(fixedDelay = 10000)
	public void testScheduled() {
		System.out.println("BaseLineTeamService.testScheduled()");
	}

	public void testScheduled1() {
		System.out.println("BaseLineTeamService.testScheduled【1】()");
	}

	// fixedDelayString属性用于给出字符串形式的毫秒数。可以在该字符串中使用spel表达式#{...}和占位符表达式${...}，但计算结果必须是一个数字。
	@Scheduled(fixedDelayString = "#{(3 * ${fixedDelay}) - ${clearCacheOffset}}")
	@CacheEvict(allEntries = true)
	public void testScheduled2() {
		System.out.println("清理缓存");
	}
}