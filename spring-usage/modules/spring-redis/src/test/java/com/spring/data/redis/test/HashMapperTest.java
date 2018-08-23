package com.spring.data.redis.test;

import static com.spring.redis.data.domain.DomainUtils.generateDept;
import static com.spring.redis.data.domain.DomainUtils.generateDepts;
import static com.spring.redis.data.domain.DomainUtils.generateLine;
import static com.spring.redis.data.domain.DomainUtils.generateLines;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.hash.BeanUtilsHashMapper;
import org.springframework.data.redis.hash.DecoratingStringHashMapper;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.format.Formatter;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.spring.redis.data.domain.BaseDept;
import com.spring.redis.data.domain.BaseLine;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class HashMapperTest {

	@Configuration
	static class Config {

		@Bean
		public RedisConnectionFactory redisConnectionFactory() {
			RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration("localhost", 63790);
			serverConfig.setDatabase(0);

			GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
			poolConfig.setMinIdle(3);
			poolConfig.setMaxIdle(5);
			poolConfig.setMaxTotal(10);
			poolConfig.setMaxWaitMillis(TimeUnit.SECONDS.toMillis(10));
			poolConfig.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(3));

			JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().clientName("maziqiang")
					.connectTimeout(Duration.ofMinutes(2)).readTimeout(Duration.ofMinutes(5)).usePooling()
					.poolConfig(poolConfig).build();

			RedisConnectionFactory connectionFactory = new JedisConnectionFactory(serverConfig, clientConfig);

			return connectionFactory;
		}

		@Bean
		public StringRedisSerializer stringRedisSerializer() {
			return new StringRedisSerializer(Charset.defaultCharset());
		}

		@Bean
		public ConversionService conversionService() {
			FormattingConversionService conversionService = new FormattingConversionService();
			conversionService.addFormatter(new DateFormatter("yyyy-MM-dd kk:mm:ss"));
			conversionService.addFormatter(new Formatter<BaseDept>() {
				@Override
				public String print(BaseDept dept, Locale locale) {
					StringJoiner joiner = new StringJoiner("@");
					String contents = joiner.add(dept.getDeptNo().toString()).add(dept.getDeptCode())
							.add(dept.getDeptName()).toString();

					return contents;
				}

				@Override
				public BaseDept parse(String text, Locale locale) throws ParseException {
					StringTokenizer tokenizer = new StringTokenizer(text, "@");
					BaseDept dept = new BaseDept();

					int index = 0;
					while (tokenizer.hasMoreTokens()) {
						String token = tokenizer.nextToken();

						if (index == 0) {
							Integer deptNo = Integer.valueOf(token);
							dept.setDeptNo(deptNo);
						} else if (index == 1) {
							dept.setDeptCode(token);
						} else if (index == 2) {
							dept.setDeptName(token);
						}
					}

					return dept;
				}
			});

			return conversionService;
		}

		@Bean
		public GenericToStringSerializer<Date> dateToStringSerializer() {
			GenericToStringSerializer<Date> dateToStringSerializer = new GenericToStringSerializer<>(Date.class);
			dateToStringSerializer.setConversionService(conversionService());

			return dateToStringSerializer;
		}

		@Bean
		public GenericToStringSerializer<BaseDept> deptToStringSerializer() {
			GenericToStringSerializer<BaseDept> deptToStringSerializer = new GenericToStringSerializer<>(
					BaseDept.class);
			deptToStringSerializer.setConversionService(conversionService());

			return deptToStringSerializer;
		}

		@Bean
		public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
			GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

			return genericJackson2JsonRedisSerializer;
		}

		@Bean
		public JdkSerializationRedisSerializer jdkSerializationRedisSerializer() {
			return new JdkSerializationRedisSerializer();
		}

		@Bean
		public RedisTemplate<BaseDept, BaseLine> deptLineTemplate() {
			RedisTemplate<BaseDept, BaseLine> template = new RedisTemplate<>();
			template.setKeySerializer(deptToStringSerializer());
			template.setValueSerializer(genericJackson2JsonRedisSerializer());
			template.setConnectionFactory(redisConnectionFactory());

			return template;
		}

		@Bean
		public RedisTemplate<Date, String> dateTemplate() {
			RedisTemplate<Date, String> template = new RedisTemplate<>();
			template.setKeySerializer(dateToStringSerializer());
			template.setValueSerializer(stringRedisSerializer());
			template.setConnectionFactory(redisConnectionFactory());

			return template;
		}

		@Bean
		public StringRedisTemplate stringRedisTemplate() {
			return new StringRedisTemplate(redisConnectionFactory());
		}

		@Bean
		public RedisTemplate<BaseDept, ?> deptTemplate() {
			RedisTemplate<BaseDept, ?> redisTemplate = new RedisTemplate<>();
			redisTemplate.setKeySerializer(deptToStringSerializer());
			redisTemplate.setHashKeySerializer(stringRedisSerializer());
			redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer());
			redisTemplate.setConnectionFactory(redisConnectionFactory());

			return redisTemplate;
		}

		@Bean
		public RedisTemplate<String, ?> testHashTemplate() {
			RedisTemplate<String, ?> redisTemplate = new RedisTemplate<>();
			redisTemplate.setKeySerializer(stringRedisSerializer());
			redisTemplate.setHashKeySerializer(deptToStringSerializer());
			redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer());
			redisTemplate.setConnectionFactory(redisConnectionFactory());

			return redisTemplate;
		}

		@Bean
		public RedisTemplate<BaseDept, BaseLine> testTemplate() {
			RedisTemplate<BaseDept, BaseLine> redisTemplate = new RedisTemplate<>();
			redisTemplate.setKeySerializer(deptToStringSerializer());
			redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer());
			redisTemplate.setConnectionFactory(redisConnectionFactory());

			return redisTemplate;
		}
	}

	@Autowired
	private RedisTemplate<BaseDept, BaseLine> deptLineTemplate;

	@Autowired
	private RedisTemplate<Date, String> dateTemplate;

	@Autowired
	private StringRedisSerializer stringRedisSerializer;

	@Autowired
	private ConversionService conversionService;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer;

	@Autowired
	private RedisTemplate<BaseDept, ?> deptTemplate;

	@Autowired
	private RedisTemplate<String, ?> testHashTemplate;

	@Autowired
	private JdkSerializationRedisSerializer jdkSerializationRedisSerializer;

	@Autowired
	private RedisTemplate<BaseDept, BaseLine> testTemplate;

	@Test
	@DirtiesContext(hierarchyMode = HierarchyMode.CURRENT_LEVEL, methodMode = MethodMode.AFTER_METHOD)
	@Ignore
	public void test() {
		BaseLine line = generateLine();

		/*
		 * HashMapper用于把一个对象转换成Map对象，这个Map中的key是对象的属性名，value是对象的属性值。
		 * 其目的是传入给HashOperations类的putAll方法，以方便的把一个POJO对象存储成redis中的hash数据结构。
		 * 
		 * HashMapper的实现类主要是BeanUtilsHashMapper和Jackson2HashMapper：
		 * 
		 * 1.BeanUtilsHashMapper固定把一个对象生成Map<String,String>的Map对象。对于对象的属性值，
		 * 都是调用属性值的toString方法来获取String类型的value。
		 * 
		 * BaseDept对象使用BeanUtilsHashMapper生成的Map的内容为:{"detpNo":"10","deptCode":
		 * "No.10","class":"com.domain.BaseDept"}
		 * 
		 * 2.Jackson2HashMapper是使用jackson构件，把一个对象生成Map<String,Object>的Map对象。
		 * 可以看到，它和BeanUtilsHashMapper的区别是，对于对象的属性值不会进行改变。例如：
		 * BaseLine对象的lineNo属性是Integer类型的，那么转换后的Map对象中，
		 * lineNo这个key的value也是Integer类型的。
		 * 
		 * BaseLine对象使用Jackson2HashMapper生成的Map的内容为:{"lineNo":10,"deptCode":
		 * "No.10","@class":"com.Domain.BaseLine","dept":{"deptNo":20,"deptCode"
		 * :"No.30","@class":"com.domain.BaseLine"}}
		 * 
		 * 注意，Jackson2HashMapper生成的Map中：
		 * 
		 * a) Map中属性值的类型就是POJO中属性的类型。
		 * 例如BaseLine中lineNo属性为Integer，那么生成的Map中，lineNo这个key的value也是Integer类型的。
		 * 
		 * b) Map中会存储一个名为@class的key，记录了这个POJO对象的类型。把这个Map传给fromHash方法时，就能根据该key，
		 * 找到要转换成的POJO对象的类型。
		 * 
		 * c) 对于POJO对象中引用的其他POJO对象，也会将其转换成Map对象。这点是BeanUtilsHashMapper所没有的。
		 * 
		 * 3.在很多情况下， RedisTemplate的hashValueSerializer是StringRedisSerializer，
		 * 这样就要求Map中的value必须是String类型的。而DecoratingStringHashMapper就是为了解决这个问题，
		 * 它把代理的HashMapper所产生的Map中的所有的key和value都使用String.valueOf的方式转换成String类型的。
		 * 
		 * 注意：HashMapper生成的Map对象，
		 * 它的key和value必须要能被RedisTemplate的hashKeySerializer和hashValueSerializer处理。
		 */
		BeanUtilsHashMapper<BaseLine> beanUtilsHashMapper = new BeanUtilsHashMapper<>(BaseLine.class);
		Map<String, String> lineHash = beanUtilsHashMapper.toHash(line);
		System.out.println(lineHash);

		Jackson2HashMapper jackson2HashMapper = new Jackson2HashMapper(false);
		// 使用DecoratingStringHashMapper装饰Jackson2HashMapper，把Jackson2HashMapper生成的Map中的value都转换成String类型的
		DecoratingStringHashMapper<Object> stringHashMapper = new DecoratingStringHashMapper<>(jackson2HashMapper);
		Map<String, String> lineHash1 = stringHashMapper.toHash(line);
		System.out.println(lineHash1);

		Map<String, Object> lineHash2 = jackson2HashMapper.toHash(line);

		dateTemplate.setHashKeySerializer(stringRedisSerializer);
		dateTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);

		Date date1 = conversionService.convert("2018-10-29 13:55:21", Date.class);
		Date date2 = conversionService.convert("2018-08-15 22:13:55", Date.class);
		Date date3 = conversionService.convert("2018-05-11 08:23:19", Date.class);

		HashOperations<Date, String, Object> opsForHash = dateTemplate.opsForHash();
		opsForHash.putAll(date1, lineHash2);
		opsForHash.putAll(date2, lineHash);

		dateTemplate.setHashValueSerializer(stringRedisSerializer);
		HashOperations<Date, String, String> opsForHash1 = dateTemplate.opsForHash();
		opsForHash1.putAll(date3, lineHash1);

	}

	@Test
	@Ignore
	public void test1() {
		ListOperations<BaseDept, BaseLine> opsForList = deptLineTemplate.opsForList();
		BaseDept dept1 = generateDept();
		opsForList.leftPushAll(dept1, generateLines(20, 30, 15, 40, 11, 9, 8, 50));
		BaseLine linePoped1 = opsForList.rightPop(dept1);
		System.out.println(linePoped1.getLineName());

		opsForList.rightPushAll(dept1, generateLines(15, 23, 99, 109, 71, 200, 33, 9, 8, 17, 66, 10));
		opsForList.trim(dept1, -5, -2);

		List<BaseLine> lineList = opsForList.range(dept1, 0, -1);
		lineList.forEach(line -> System.out.println(line.getLineName() + ":" + line.getDept().getDeptName()));
	}

	@Test
	@Ignore
	public void test2() {
		SetOperations<Date, String> opsForSet = dateTemplate.opsForSet();
		Date date1 = conversionService.convert("2018-10-01 03:20:11", Date.class);
		Date date2 = conversionService.convert("2018-05-27 05:17:21", Date.class);
		Date date3 = conversionService.convert("2018-09-12 12:10:53", Date.class);
		Date date4 = conversionService.convert("2018-03-20 23:15:40", Date.class);

		opsForSet.add(date1, "zhangsan", "lisi", "wangwu", "zhaoliu");
		opsForSet.add(date2, "yuguofei", "zhangsan", "yuguofei", "wangtianlong", "wangtianlong", "maziqiang", "wangwu",
				"zhangsan");
		opsForSet.add(date3, "wangwu", "wangwu", "liukaiqian", "bailu", "bailu", "hewenbin", "zhangsan");
		opsForSet.add(date4, "zhangsan", "wangwu", "wangyin");

		opsForSet.difference(date2, Sets.newHashSet(date1, date3, date4)).forEach(System.out::println);
		opsForSet.intersect(date1, Sets.newHashSet(date2, date3, date4)).forEach(System.out::println);

		Date now = new Date();
		opsForSet.unionAndStore(date1, Sets.newHashSet(date2, date3, date4), now);
		opsForSet.members(now).forEach(System.out::println);

	}

	@Test
	@DirtiesContext(hierarchyMode = HierarchyMode.CURRENT_LEVEL, methodMode = MethodMode.AFTER_METHOD)
	@Ignore
	public void test3() {
		HashOperations<String, String, Object> opsForHash = stringRedisTemplate.opsForHash();

		BaseLine line = generateLine();
		BeanUtilsHashMapper<BaseLine> hashMapper = new BeanUtilsHashMapper<>(BaseLine.class);
		Map<String, String> lineMap = hashMapper.toHash(line);

		opsForHash.putAll("line", lineMap);

		/*
		 * 在创建Jackson2HashMapper时，可以传入自己创建的ObjectMapper。
		 * 
		 * dateFormat()方法是设置POJO对象里所有Date类型的属性的默认格式化方式。但POJO类中Date属性上的@
		 * JsonFormat的注解会覆盖对该属性的默认格式化方式
		 */
		ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().dateFormat(DateFormat.getDateInstance()).build();

		/*
		 * flattern属性的作用：
		 * 
		 * 假设POJO类的属性为:int lineNo,String lineCode,String lineName,BaseDept dept
		 * 
		 * 1.如果flattern的属性值为false，那么toHash(BaseLine)创建的Map的值{lineNo:30,lineCode:
		 * No.30,lineName:30线路,dept:{deptNo:20,deptName:20公司}}。
		 * 其中dept属性的值也是一个Map对象。
		 * 
		 * 2.如果flattern的属性值为true，那么toHash(BaseLine)创建的Map的值{lineNo:30,lineCode:
		 * No.30,lineName:30线路,dept.deptNo:20,dept.deptName:20公司}
		 * 
		 * 可以看到，flattern主要是控制BaseLine中引用的对象（在这里就是BaseDept对象）的处理方式
		 */
		Jackson2HashMapper jacksonMapper = new Jackson2HashMapper(objectMapper, true);
		/*
		 * 使用Jackson2HashMapper有一点要注意的是：Jackson2HashMapper转换的Map中，
		 * value是Object类型的，而非String类型的。因此，假设POJO类BaseLine中lineNo属性为Integer，
		 * 那么将BaseLine对象转换成Map对象后，lineNo这个key的value值是Integer类型的11，而不是String类型的。
		 */
		Map<String, Object> lineMap1 = jacksonMapper.toHash(line);
		System.out.println(lineMap1);
		/*
		 * 由于lineMap1中，lineNo这个key的value是Integer类型的，
		 * 而RedisTemplate中hashValueSerializer值是StringRedisSerializer，
		 * 把lineNo的value传给StringRedisSerializer则会报错。
		 * 
		 * 因此下面这行代码执行时则会抛出异常
		 */
		// opsForHash.putAll("line1", lineMap1);

		/*
		 * 但是如果我们重新设置StringRedisTemplate的hashValueSerializer，结果就不一样了。
		 * 我们把HashValueSerializer设置为GenericJackson2JsonRedisSerializer。
		 * 
		 * 由于GenericJackson2JsonRedisSerializer能处理任意类型，
		 * 因此无论Map中的value是String还是Integer还是Boolean,
		 * GenericJackson2JsonRedisSerializer都可以处理。
		 */
		stringRedisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);
		opsForHash.putAll("line1", lineMap1);

		/*
		 * DecoratingStringHashMapper就是用来解决Jackson2HashMapper的toHash方法转换后的Map中的value是Object
		 * ，而非String的问题。
		 * 
		 * DecoratingStringHashMapper会代理一个HashMapper，在toHash方法时，
		 * 先让代理的HashMapper来执行toHash方法。
		 * 然后对代理的HashMapper所返回的Map对象的key和value进行String.valueOf操作，创建一个Map<String,
		 * String>的对象。
		 */
		DecoratingStringHashMapper<Object> decoratingMapper = new DecoratingStringHashMapper<>(jacksonMapper);
		Map<String, String> lineMap2 = decoratingMapper.toHash(line);

		/*
		 * 由于lineMap2中的key和value都是String类型的，
		 * 而RedisTemplate中的hashKeySerializer和hashValueSerializer都是StringRedisSerializer
		 * ，因此可以正常进行序列化操作。
		 */
		opsForHash.putAll("line1", lineMap2);
	}

	@Test
	@Ignore
	public void test5() {
		ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().dateFormat(DateFormat.getDateInstance()).build();
		Jackson2HashMapper jackson2HashMapper = new Jackson2HashMapper(objectMapper, true);

		DecoratingStringHashMapper<Object> stringHashMapper = new DecoratingStringHashMapper<>(jackson2HashMapper);

		BaseLine line = generateLine();

		Map<String, String> lineHash = stringHashMapper.toHash(line);
		System.out.println(lineHash);

		HashOperations<String, String, String> opsForHash = stringRedisTemplate.opsForHash();
		opsForHash.putAll("line", lineHash);
		opsForHash.put("line", "test", "haha");
		opsForHash.put("line", "user", "maziqiang");
		opsForHash.delete("line", "test");

		Long lineNo = opsForHash.increment("line", "lineNo", 99);
		assert lineNo.equals(Long.valueOf(opsForHash.get("line", "lineNo")));

		Long keyCounts = opsForHash.size("line");
		System.out.println(keyCounts);

		StringJoiner keyJoiner = new StringJoiner(",", "key共有：【", "】");
		opsForHash.keys("line").forEach(keyJoiner::add);
		System.out.println(keyJoiner.toString());

		StringJoiner valueJoiner = new StringJoiner(",", "value共有：【", "】");
		opsForHash.multiGet("line", opsForHash.keys("line")).forEach(valueJoiner::add);
		System.out.println(valueJoiner.toString());

		StringJoiner keyValueJoiner = new StringJoiner(",", "key和value共有：【", "】");
		opsForHash.entries("line").forEach((key, value) -> keyValueJoiner.add(key + "_" + value));
		System.out.println(keyValueJoiner.toString());
	}

	@Test
	/*
	 * 下面这个方法演示了把一个POJO对象转换成Map对象，然后存储到redis的hash数据结构的key中。
	 * 然后再从redis中读取该key的所有hashKey和hashValue，生成一个Map对象，最后使用这个Map对象还原回POJO对象。
	 * 
	 * 注意：此时RedisTemplate中的hashValueSerializer必须是一个能处理任意类型的，
	 * 因为Jackson2HashMapper生成的Map对象，其value可能是任何类型的。
	 */
	@Ignore
	public void test6() {
		BaseLine line = generateLine();
		BaseDept dept = generateDept();

		Jackson2HashMapper jackson2HashMapper = new Jackson2HashMapper(false);
		// 1.使用Jackson2HashMapper，将POJO对象转换为Map<String,Object>对象
		Map<String, Object> lineHash = jackson2HashMapper.toHash(line);

		HashOperations<BaseDept, String, Object> opsForHash = deptTemplate.opsForHash();
		// 2.将转换后的Map对象存储到redis中，存储的肯定是redis的hash数据结构
		opsForHash.putAll(dept, lineHash);

		// 3.从redis中获取该key的hash数据结构的所有hashKey和hashValue
		Map<String, Object> hashFromRedis = opsForHash.entries(dept);
		System.out.println(hashFromRedis);

		// 4.使用Jackson2HashMapper，根据Map对象还原回POJO对象
		Object lineFromRedis = jackson2HashMapper.fromHash(hashFromRedis);
		BaseLine line1 = BaseLine.class.cast(lineFromRedis);
		System.out.println(line1.getLineName() + ":" + line1.getDept().getDeptName());
	}

	@Test
	@Ignore
	public void test7() {
		HashOperations<String, BaseDept, Object> opsForHash = testHashTemplate.opsForHash();
		BaseDept dept1 = generateDept();
		BaseLine line1 = generateLine();

		BaseDept dept2 = generateDept();
		Date date = new Date();

		BaseDept dept3 = generateDept();
		BaseDept dept4 = generateDept();

		BaseDept[] deptArray = { dept1, dept2, dept3, dept4 };

		opsForHash.put("hash", dept1, line1);
		opsForHash.put("hash", dept2, date);
		opsForHash.put("hash", dept3, "hello world");
		opsForHash.put("hash", dept4, deptArray);

		Object value1 = opsForHash.get("hash", dept1);
		Object value2 = opsForHash.get("hash", dept2);
		Object value3 = opsForHash.get("hash", dept3);
		Object value4 = opsForHash.get("hash", dept4);

		assert date.equals(value2);

		System.out.println(value1);
		System.out.println(value2);
		assert value3 instanceof String;
		if (ObjectUtils.isArray(value4)) {
			Arrays.stream((Object[]) value4).filter(obj -> BaseDept.class.isInstance(obj))
					.map(obj -> BaseDept.class.cast(obj)).forEach(dept -> System.out.println(dept.getDeptName()));
		}

	}

	@Test
	@DirtiesContext(hierarchyMode = HierarchyMode.CURRENT_LEVEL, methodMode = MethodMode.AFTER_METHOD)
	@Ignore
	public void test8() {
		deptTemplate.setHashValueSerializer(jdkSerializationRedisSerializer);
		HashOperations<BaseDept, String, Object> opsForHash = deptTemplate.opsForHash();

		BaseLine line = generateLine();
		BaseDept dept = generateDept();

		Jackson2HashMapper jackson2HashMapper = new Jackson2HashMapper(false);
		Map<String, Object> lineHash = jackson2HashMapper.toHash(line);

		opsForHash.putAll(dept, lineHash);

		Map<String, Object> lineHashFromRedis = opsForHash.entries(dept);
		Object obj = jackson2HashMapper.fromHash(lineHashFromRedis);
		System.out.println(obj.getClass());

		BaseDept dept1 = generateDept();
		opsForHash.put(dept1, "zhangsan", new Date());
		opsForHash.put(dept1, "lisi", Calendar.getInstance());
		opsForHash.put(dept1, "hello", dept);
		opsForHash.put(dept1, "world", line);
		opsForHash.put(dept1, "user", ArrayUtils.toArray(generateDepts(3), generateLines(5)));

		List<Object> valueList = opsForHash.multiGet(dept1, opsForHash.keys(dept1));
		System.out.println(valueList);
	}

	@Test
	@Ignore
	public void test9() {
		SetOperations<BaseDept, BaseLine> opsForSet = testTemplate.opsForSet();

		BaseDept dept1 = generateDept(37);
		BaseDept dept2 = generateDept(13);

		opsForSet.add(dept1, generateLines(10, 20, 99, 501, 800, 5));
		opsForSet.add(dept2, generateLines(13));

		Set<BaseLine> lines1 = opsForSet.members(dept1);
		Set<BaseLine> lines2 = opsForSet.members(dept2);

		lines1.forEach(line -> System.out.println(line.getLineName() + ":" + line.getDept().getDeptName()));
		lines2.forEach(line -> System.out.println(line.getLineName() + ":" + line.getDept().getDeptName()));
	}

	@Test
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	@Ignore
	public void test10() {
		testTemplate.setHashKeySerializer(stringRedisSerializer);
		testTemplate.setHashValueSerializer(jdkSerializationRedisSerializer);

		HashOperations<BaseDept, String, Object> opsForHash = testTemplate.opsForHash();

		BaseDept dept = generateDept();

		// 手动地把一个POJO对象的所有属性存储到hash中
		opsForHash.put(dept, "deptNo", dept.getDeptNo());
		opsForHash.put(dept, "deptName", dept.getDeptName());
		opsForHash.put(dept, "deptCode", dept.getDeptCode());
		opsForHash.put(dept, "modifyDate", dept.getModifyDate());
		opsForHash.put(dept, "isRun", dept.getIsRun());

		// 手动从redis中获取hash的每一个hashKey
		Integer deptNo = cast(opsForHash.get(dept, "deptNo"), Integer.class);
		String deptName = cast(opsForHash.get(dept, "deptName"), String.class);
		String deptCode = cast(opsForHash.get(dept, "deptCode"), String.class);
		Date modifyDate = cast(opsForHash.get(dept, "modifyDate"), Date.class);
		Boolean isRun = cast(opsForHash.get(dept, "isRun"), Boolean.class);

		System.out.println(deptNo);
		System.out.println(deptName);
		System.out.println(deptCode);
		System.out.println(modifyDate);
		System.out.println(isRun);
	}

	private <T> T cast(Object obj, Class<T> clazz) {
		if (clazz.isInstance(obj)) {
			return clazz.cast(obj);
		} else {
			return null;
		}
	}

	@Test
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	public void test11() {
		stringRedisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);

		BaseLine line = generateLine(6666, 9999);
		
		/*
		 * 关于Jackson2HashMapper(flattern=true)的一个细节。
		 * 
		 * 如果Jackson2HashMapper的flattern属性设置为true，那么使用它将POJO对象转换成Map对象时，
		 * POJO对象中的Date对象没有被生成到Map中。例如如果flattern=true，那么使用它把BaseLine对象转换为Map对象时，
		 * Map对象中没有Date类型的modifyDate属性。
		 * 
		 * 暂时不太清楚为什么，不过应尽量避免使用flattern=true的设置。
		 */
		HashMapper<Object, String, Object> hashMapper = new Jackson2HashMapper(false);
		Map<String, Object> lineHash = hashMapper.toHash(line);

		HashOperations<String, String, Object> opsForHash = stringRedisTemplate.opsForHash();
		opsForHash.putAll("test1", lineHash);

		Map<String, Object> hashFromRedis = opsForHash.entries("test1");
		BaseLine lineFromRedis = (BaseLine) hashMapper.fromHash(hashFromRedis);

		System.out.println(lineFromRedis.getLineName() + ":" + lineFromRedis.getDept().getDeptName());

	}

}