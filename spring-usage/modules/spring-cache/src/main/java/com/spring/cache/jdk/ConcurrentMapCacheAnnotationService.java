package com.spring.cache.jdk;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

@Component
// @EnableCaching中的order属性决定了CacheInterceptor在责任链对象的什么位置。值越小，该通知对象则越在责任链的靠前的位置，责任链就越先执行这个通知对象。
@EnableCaching(order = 1)
/*
 * 设置@Cacheable注解默认提供的cacheNames，如果@Cacheable注解没有给出cacheNames属性，则使用@
 * CacheConfig提供的默认的cacheNames。但是如果@Cacheable注解给出了cacheNames属性，则使用@
 * Cacheable注解提供的cacheNames。（注意：适用于@Cache*系列的全部注解，不仅是@Cacheable）
 */
@CacheConfig(cacheNames = "date_region")
public class ConcurrentMapCacheAnnotationService {

	@Cacheable(cacheNames = "date_region")
	public String cacheableService1(Date date) {
		return DateFormat.getDateTimeInstance().format(date);
	}

	@Cacheable
	public Date cacheableService2(String words) throws ParseException {
		return DateFormat.getDateTimeInstance().parse(words);
	}

	@CacheEvict(allEntries = false, key = "#date?.time", beforeInvocation = false)
	public void evictSingleCache(Date date) {
	}

	/*
	 * unless属性的意义是，有权利否决将调用连接点方法的返回结果存储到Cache中。unless属性是在调用连接点方法后才会进行判断的。
	 */
	@CachePut(cacheNames = "date_region", key = "#date.time", unless = "#date.month==10", condition = "#date.after(new java.util.Date())")
	public String forceUpdateCache(Date date) {
		/*
		 * 由于当前类被@Component注解，因此直接调用cacheableService1方法，
		 * 实际上调用的是目标方法cacheableService1，而不是代理方法cacheableService1。
		 * 如果是调用代理的cacheableService1方法，那么该方法会先尝试从容器中获取标识符为cacheableService1的<
		 * bean>实例，如果没有获取到，才调用目标方法
		 */
		return cacheableService1(date);
	}

	/*
	 * 先清除缓存中的对应key的内容，然后再判断缓存中是否有该key，此时肯定没有，然后就调用目标对象，将目标对象的返回值存储到缓存中。
	 * 因此以下@Caching配置相当于@CachePut操作。
	 */
	@Caching(cacheable = @Cacheable(cacheNames = "date_region", key = "#date.time"), evict = @CacheEvict(cacheNames = "date_region", key = "#date.time", beforeInvocation = true, allEntries = false))
	public String evictThenPut(Date date) {
		return cacheableService1(date);
	}

	@Cacheable(cacheNames = "dept_region")
	public String cacheableService3(Date date) {
		return cacheableService1(date);
	}

	/*
	 * @Cache*注解的sync属性为true后，需要缓存操作方法只能对应一个Cache，否则会报错。
	 * sync的作用是，当如果有多个线程同时调用该方法，该方法在访问同一个Cache时，是否进行阻塞。即同一个时刻，只能有一个线程能访问该Cache，
	 * 这个时刻其他访问该Cache的线程都会被阻塞。直到访问Cache的线程执行完成后，下一个线程才能继续访问Cache。
	 * 
	 * sync主要解决的是，如果多个线程同时访问cacheableService1方法时，多个线程同时进入CacheInteceptor，
	 * 同时从Cache中获取key对应的value，由于此时Cache中没有该key，因此，在这些线程中，该Cache都判断出没有这个key。
	 * 此时多个线程直接就都调用目标方法了。但实际上，这些线程中的key都是同一个。
	 * 
	 * 正确的顺序应是：第一个线程访问Cache，发现没有对应的key，调用目标方法，将目标方法返回结果注册到Cache。
	 * 之后第二个线程才能再访问Cache，发现已经有对应的key了，从Cache中获取数据。依此类推。
	 * 
	 * sync属性使对多个线程对同一个Cache的访问串行化，多个线程同时访问同一个Cache时，是串行化访问的。
	 * 
	 * 注意：只有@Cacheable注解有sync属性，@CachePut和@CacheEvict没有
	 */
	@Cacheable(key = "#date.time", sync = true)
	public String cacheableService4(Date date) {
		return cacheableService1(date);
	}

	/*
	 * 每一个@Cacheable、@CachePut、@CacheEvict都代表着在CacheInteceptor中要执行的一次缓存操作。
	 * 在每一个缓存操作中可以独立设置cacheNames、cacheResolver、cacheManager、keyGenerator。
	 * 这就意味着，在一个方法中的多个缓存操作，每个缓存操作可以设置独立的：
	 * 
	 * 1.keyGenerator，来决定这个缓存操作根据参数值获取key的方式。默认使用的是SimpleKeyGenerator
	 * 2.cacheResolver，来指定这个缓存操作获取要访问的Cache对象的方式。默认的是SimpleCacheResolver
	 * 3.cacheNames，来决定这个缓存操作所要访问的Cache对象的name。
	 * 4.cacheManager，来决定这个缓存操作使用哪个CacheManager来获取Cache对象。
	 * 5.condition，来决定这个缓存操作是否应该被执行（只有在condition为true，缓存操作才会被执行）
	 * 
	 * 注意：如果没有设置cacheResolver，则spring会使用默认的SimpleCacheResolver获取Cache，
	 * 此时SimpleCacheResolver会使用缓存操作上的cacheNames属性和cacheManager属性来获取Cache对象。
	 * 而如果设置了cacheResolver，
	 * 则只会使用这个cacheResolver关联的cacheNames和cacheManager来获取Cache对象。
	 * 此时缓存操作上设置的cacheNames和cacheManager则无效了。
	 * 
	 * 因此，对于每一个缓存操作：
	 * 
	 * 1.都有一个key。是将参数值传入给缓存操作中的key或keyGenerator，由其生成出来的
	 * 2.都有一个要访问的Cache对象的集合。是根据缓存操作中的cacheResolver获得这个Cache对象集合
	 * 3.都有condition属性。只有condition属性值的spel表达式的计算结果为true，这个缓存操作才是可以执行的
	 * 
	 * 也就是说，对于每一个缓存操作，都要想到：
	 * 
	 * 1.这个缓存操作在当前情况下应不应该执行（根据condition属性判断）
	 * 2.这个缓存操作生成的key是什么（根据key或keyGenerator属性判断，默认使用SimpleKeyGenerator）
	 * 3.这个缓存操作要访问的Cache集合是什么（根据cacheResolver属性判断，默认使用SimpleCacheResolver）
	 * 
	 * CacheInteceptor实际的执行流程：（假设每一个缓存操作都满足condition要求，也就是每一个缓存操作都要被执行）
	 * 
	 * 1.选取@CacheEvict中,beforeInvocation=true的缓存操作。对于每一个找到的缓存操作，都使用缓存操作中的key，
	 * 来清除缓存操作对应的Cache集合中的对应key和value
	 * 
	 * 2.选取@Cacheable缓存操作。对于每一个找到的缓存操作，都尝试使用缓存操作中的key，从该缓存操作对应的Cache集合中的获取value。
	 * 
	 * 3.如果任意一个@Cacheable缓存操作可以获取value，则使用这个value。否则，将所有@
	 * Cacheable缓存操作放到CachePut集合中
	 * 
	 * 4.决定是否要调用目标方法：（实际是调用责任链对象，由责任链对象最终调用目标方法）
	 * 只要有@CachePut操作，就调用目标方法。如果没有@CachePut操作，则根据是否从缓存中获取了value，来决定是否调用目标方法。
	 * 至此已经获得value了（无论是从Cache中获取的，还是调用目标方法返回的）。
	 * 
	 * 5.将所有@CachePut缓存操作放到CachePut集合中
	 * 
	 * 6.遍历CachePut集合中的所有缓存操作，对于每一个缓存操作，都会使用这个缓存操作对应的key和我们获得的value，
	 * 将这个key和value插入/更新到缓存操作对应的Cache集合中
	 * 
	 * 7.获取@CacheEvict中,beforeInvocation=false的缓存操作。对于每一个找到的缓存操作，都使用缓存操作中的key，
	 * 来清除缓存操作对应的Cache集合中的key和value
	 * 
	 * 简化的说明为：
	 * 
	 * 1.@CacheEvict(beforeInvocation=true)进行缓存清除
	 * 
	 * 2.@Cacheable尝试从缓存获取value
	 * 
	 * 3.如果没有获取到，则将每一个@Cacheable放到CachePut集合中
	 * 
	 * 4.决定是否调用目标方法：如果有@CachePut，则必定调用目标方法。如果没有，那么如果第二步获取到value了，则调用目标方法，
	 * 否则不调用目标方法。至此已经获得value了（无论是从Cache中获取的，还是调用目标方法返回的）。
	 * 
	 * 5.将@CachePut也放到CachePut集合中
	 * 
	 * 6.遍历这个CachePut集合中的缓存操作，把value插入/更新到每一个缓存操作对应的Cache集合中
	 * 
	 * 7.@CacheEvict(beforeInvocation=false)进行缓存清除
	 */
	@Caching(cacheable = { @Cacheable, @Cacheable("dept_region"), @Cacheable("line_region") }, evict = {
			@CacheEvict(beforeInvocation = true), @CacheEvict(cacheNames = "dept_region", beforeInvocation = false) })
	public String cachingService1(Date date) {
		return cacheableService1(date);
	}

	@Cacheable(cacheNames = "line_region")
	public String cacheableService5(Date date) {
		return cacheableService1(date);
	}

	@Caching(cacheable = { @Cacheable, @Cacheable(cacheNames = { "dept_region",
			"line_region" }, condition = "#source.before(new java.util.Date())", unless = "#source.month==10") })
	public String cachingService2(Date source) {
		return cacheableService1(source);
	}

	@Caching(put = { @CachePut, @CachePut(cacheNames = { "dept_region", "line_region" }) }, evict = {
			@CacheEvict(beforeInvocation = false, condition = "#time.after(new java.util.Date())"),
			@CacheEvict(beforeInvocation = false, cacheNames = { "dept_region",
					"line_region" }, condition = "#time.after(new java.util.Date())") })
	public String cachingService3(Date time) {
		return cacheableService1(time);
	}

	@Caching(cacheable = { @Cacheable, @Cacheable(cacheResolver = "cacheResolver") })
	public String cacheableService6(Date date) {
		return cacheableService1(date);
	}

}
