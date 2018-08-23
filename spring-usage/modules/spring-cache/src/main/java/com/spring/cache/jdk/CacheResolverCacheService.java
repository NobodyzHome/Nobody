package com.spring.cache.jdk;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

// 设置@Component注解，是为了该类可以被扫描，注册到容器中
@Component
// 设置@EnableCaching注解，是为了当扫描该类后，让容器启动对@Cache*注解的支持
@EnableCaching
// 设置@CacheConfig注解，是为了对该类的所有@Cache*注解给出默认的配置
@CacheConfig(cacheResolver = "cacheResolver")
public class CacheResolverCacheService {

	/*
	 * 为什么@Cacheable等注解不需要强制给出cacheNames属性？因为有了CacheResolver，现在spring不是固定从@
	 * Cacheable注解的cacheNames属性中获取Cache集合。而是通过使用CacheResolver来获取此次操作对应的Cache集合。
	 * 
	 * spring在context包中仅给出两个CacheResolver的实现类，
	 * 即SimpleCacheResolver和NamedCacheResolver。在context-
	 * support包中也给出了其他一些CacheResolver的实现类。我们也可以自己实现CacheResolver接口，
	 * 来提供自定义的获取Cache的方式。
	 * 
	 * 如果没有提供CacheResolver，那么spring默认使用的SimpleCacheResolver，
	 * 它会从当前缓存操作注解中获取cacheNames属性，然后逐一传入给当前缓存操作的CacheManger，获取Cache对象集合。
	 * 
	 * 但我们也可以使用CacheResolver的另一个实现类NamedCacheResolver，该类在获取Cache集合时，
	 * 使用的是固定cacheNames，将这些cacheName交由指定的CacheManager，获取Cache集合。因此，
	 * 此时获取的Cache集合就与@Cacheable注解中的cacheNames属性无关了，此时即使给出了@
	 * Cacheable中cacheNames属性，spring也不会使用这个cacheNames来获取Cache集合。
	 * 
	 * Since Spring 4.1, the value attribute of the cache annotations are no
	 * longer mandatory since this particular information can be provided by the
	 * CacheResolver regardless of the content of the annotation.
	 */
	@Cacheable(key = "#date.time")
	public String cacheableService(Date date) {
		return DateFormat.getDateTimeInstance().format(date);
	}

	@Cacheable(cacheNames = "date_region")
	public Date cacheableService2(String words) throws ParseException {
		return DateFormat.getDateTimeInstance().parse(words);
	}

	@CachePut(key = "#date.time")
	public String forceUpdateCache(Date date) {
		return cacheableService(date);
	}

	/*
	 * 注意：CacheEvict也会调用目标方法，但不会将目标方法的返回结果保存到Cache中。
	 * 
	 * CacheEvict允许方法的返回值类型为void，因为可以将这个方法仅作为清除Cache的触发器。该方法的结果不会与Cache产生交互。
	 * 但Cacheable和CachePut则需要将目标方法的返回结果存储到Cache中，因此，Cacheable和CachePut所注解的方法，
	 * 必须有返回值。
	 * 
	 * It is important to note that void methods can be used with @CacheEvict -
	 * as the methods act as triggers, the return values are ignored (as they
	 * don’t interact with the cache) - this is not the case with @Cacheable
	 * which adds/updates data into the cache and thus requires a result.
	 */
	@CacheEvict(key = "#date.time", beforeInvocation = true, allEntries = false)
	public void evictSpecificKey(Date date) {
	}

	/*
	 * 关于如何使用方法产生的key来判断Cache中是否有对应的key和value？
	 * 假设spring使用方法的参数值生成出的key为key1，而Cache中当前要判定的key为key2，那么只有当key1.
	 * equals(key2)&&key1.hashcode()==key2.hashcode()时，才会认为当前生成的key在Cache中已存在。
	 * 
	 * 关于SimpleKeyGennerator是如何生成key的？
	 * 1.对于没有参数的方法，不管是调用哪个方法，获得的都是SimpleKey.EMPTY这个key。因此，在同一个Cache的两个没有参数的方法，
	 * 缓存记录的第一个方法返回的key和value，会被第二个方法返回的key和value覆盖，因为这两个方法产生的key是相同的。
	 * 
	 * 2.对于只有一个参数的方法，获得的key是这个参数值本身。那么这个参数值的类的eqauls和hashcode方法应该被覆盖，
	 * 提供在什么时候认为两个对象在逻辑意义上是相同的。
	 * 
	 * 3.对于有多个参数的方法，获得的key是使用这些参数值创建的SimpleKey对象。SimpleKey对象在和Cache中的其他key比较的时候，
	 * 只有要比较的key也是SimpleKey类型，并且SimpleKey对象中的参数值数组中的每一个元素，
	 * 也和要比较的SimpleKey对象中的参数值数组中的每一个元素完全相同时（即equals返回true并且hashcode相同），
	 * 才认为这个SimpleKey对象和要比较的Cache中的key相同。因此，方法的参数值的类，也应对eqauls和hashcode方法进行覆盖。
	 * 
	 * 总结就是，为了使spring判断当前方法的参数值是否在缓存的key中，方法的参数值的类型需要覆盖equals和hashcode方法，
	 * 给出两个对象在逻辑上是相同的判断方法。
	 */
	@CacheEvict(key = "#date.time", beforeInvocation = false, allEntries = true)
	public String evictAllKey(Date date) {
		return cacheableService(date);
	}

	/*
	 * 其实不用给出@Cacheable的key="#date.time"的配置，因为Date类已经覆盖了equals和hashcode方法，
	 * 给出了逻辑上两个Date对象相同的判断（即时间相同）。因此，使用Date作为Cache的key也是可以的。
	 */
	@Cacheable(unless = "#date.month==11")
	public String cacheableServiceUnless(Date date) {
		return cacheableService(date);
	}
}
