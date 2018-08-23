package com.spring.data.redis.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.SortParameters.Order;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.query.SortQuery;
import org.springframework.data.redis.core.query.SortQueryBuilder;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Lists;
import com.spring.redis.data.domain.BaseDept;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-data-redis-test.xml")
public class BasicTest {

	@Autowired
	private JedisConnectionFactory redisConnection;

	@Autowired
	private RedisTemplate<String, BaseDept> redisTemplate;

	@Resource(name = "redisTemplate1")
	private RedisTemplate<Object, Object> template1;

	@Value("2017-12-25 14:28:00")
	@DateTimeFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	private Date date;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private JdkSerializationRedisSerializer redisSerializer;

	@Test
	@Ignore
	public void test1() {
		RedisConnection conn = redisConnection.getConnection();
		conn.set("shaoShuai".getBytes(), "wuha".getBytes());

		byte[] value = conn.get("shaoShuai".getBytes());
		System.out.println(new String(value));
	}

	@Test
	@Ignore
	public void test2() {
		BaseDept value = new BaseDept(123, "123dept", "123公司");
		value.setModifyDate(new Date());
		value.setIsRun(false);

		redisTemplate.opsForValue().set("dept", value);
		BaseDept dept = redisTemplate.opsForValue().get("dept");
		System.out.println(dept);
	}

	@Test
	@Ignore
	public void test3() {
		byte[] contents = template1.execute((RedisConnection connection) -> {
			connection.set("she".getBytes(), "jay123".getBytes());
			return connection.get("she".getBytes());
		});

		System.out.println(new String(contents));

		ValueOperations<Object, Object> valueOption = template1.opsForValue();
		valueOption.set("hayo", new BaseDept(123, "123dept", "123公司"));
		valueOption.set("loha", "coach");
		valueOption.set("time", new Date());

		Object result = valueOption.get("hayo");
		System.out.println(result instanceof BaseDept);
		System.out.println(valueOption.get("time") instanceof Date);
	}

	@Test
	@Ignore
	public void test4() {
		BoundValueOperations<String, String> timeOps = stringRedisTemplate.boundValueOps("time");
		System.out.println(timeOps.get());
		timeOps.set("today is a good day", 1, TimeUnit.MINUTES);
		System.out.println(timeOps.get());

		ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
		ops.append("time", "what a wonderful day!");
		String oldValue = ops.getAndSet("user", "wangdazhuang");
		System.out.println(oldValue);

		Map<String, String> map = new HashMap<>();
		map.put("yoga", "nice name");
		map.put("since", "long times age");
		map.put("john", "a bad boy");

		Boolean isSuccess = ops.multiSetIfAbsent(map);
		System.out.println(isSuccess);

		List<String> values = ops.multiGet(Lists.newArrayList("yoga", "since", "john"));
		System.out.println(values);

		Long yoga = ops.size("yoga");
		System.out.println(yoga);

		ops.set("yoga", "tomato", 5);
		System.out.println(ops.get("yoga"));

		ops.set("dumpling", "hello world", 6);
		System.out.println(ops.get("dumpling"));

		ops.set("dumpling", "nice words", 20);
		System.out.println(ops.get("dumpling"));

		template1.delete("books");
		ops.increment("books", 5);
		System.out.println(ops.get("books"));

		ops.increment("books", -3);
		System.out.println(ops.get("books"));

		ops.increment("books", 3.1415);
		System.out.println(ops.get("books"));

		ops.increment("books", -1.217513);
		System.out.println(ops.get("books"));

		Boolean setSeccess = ops.setIfAbsent("newspaper", "we need a hero,today is a good day");
		if (setSeccess) {
			System.out.println(ops.get("newspaper", 3, -5));
			System.out.println(ops.get("newspaper", 10, 13));
			System.out.println(ops.get("newspaper", 10, 120));
			System.out.println(ops.get("newspaper", -13, 26));
		}

		String john = ops.getAndSet("john", "约翰");
		System.out.println(john);
		System.out.println(ops.get("john"));
	}

	@Test
	@Ignore
	public void test5() {
		Set<String> keys = stringRedisTemplate.keys("*");
		keys.forEach(key -> {
			if (stringRedisTemplate.type(key).equals(DataType.STRING)) {
				System.out.println(key + ":" + stringRedisTemplate.opsForValue().get(key));
				stringRedisTemplate.expire(key, 10, TimeUnit.SECONDS);
			}
		});
	}

	@Test
	@Ignore
	public void test6() {
		byte[] johnBytes = stringRedisTemplate.dump("john");
		stringRedisTemplate.delete("john");

		if (!stringRedisTemplate.hasKey("john")) {
			stringRedisTemplate.restore("john", johnBytes, 10, TimeUnit.SECONDS);
			stringRedisTemplate.persist("john");

			String john = stringRedisTemplate.boundValueOps("john").get();
			System.out.println(john);
		}
	}

	@Test
	@Ignore
	public void test7() {
		ListOperations<String, String> listOps = stringRedisTemplate.opsForList();
		listOps.leftPushAll("name-list", "zhangsan", "lisi", "王五", "zhaoliu", "a good day");
		System.out.println(listOps.range("name-list", 0, -1));
		System.out.println(listOps.range("name-list", -2, -1));
	}

	@Test
	@Ignore
	public void test8() {
		stringRedisTemplate.delete(stringRedisTemplate.keys("*"));

		ListOperations<String, String> listOps = stringRedisTemplate.opsForList();
		Long counts = listOps.leftPushAll("name-list", "this is a good day", "zhang san", "li si", "wang wu",
				"zhao liu", "wa ha ha", "hu lu wa");
		System.out.println(counts);

		counts = listOps.rightPushAll("name-list", "tom", "jerry", "shu ke", "bei ta", "wu qing");
		System.out.println(counts);

		Long listSize = listOps.size("name-list");
		System.out.println(listSize);

		listOps.set("name-list", -3, "jin gang");
		String words = listOps.index("name-list", -3);
		System.out.println(words);

		System.out.println(listOps.range("name-list", 0, -1));
		listOps.trim("name-list", 1, -3);
		System.out.println(listOps.range("name-list", 0, -1));

		String poped = listOps.leftPop("name-list");
		System.out.println(poped);

		poped = listOps.rightPop("name-list");
		System.out.println(poped);

		String poped1 = listOps.rightPopAndLeftPush("name-list", "name-copy");
		System.out.println(poped1);

		poped1 = listOps.rightPopAndLeftPush("name-list", "name-copy");
		System.out.println(poped1);

		System.out.println(listOps.range("name-list", 0, -1));
		System.out.println(listOps.range("name-copy", 0, -1));

		Long removed = listOps.remove("name-list", 0, "zhang san");
		System.out.println(removed);
		System.out.println(listOps.range("name-list", 0, -1));
	}

	@Test
	@Ignore
	public void test9() {
		SetOperations<String, String> opsForSet = stringRedisTemplate.opsForSet();
		Long cityGroup1Count = opsForSet.add("city-group-1", "da lian", "tian jin", "shang hai", "guang zhou");
		System.out.println(cityGroup1Count);

		cityGroup1Count = opsForSet.add("city-group-1", "hang zhou", "shang hai", "da lian", "nei meng", "tian jin",
				"wu han");
		System.out.println(cityGroup1Count);

		opsForSet.add("city-group-2", "bei jing", "da lian", "nan jing", "shang hai");
		opsForSet.add("city-group-3", "hang zhou", "wu han", "qing dao", "ha er bin", "tian jin");

		opsForSet.difference("city-group-1", "city-group-2").forEach(System.out::println);
		System.out.println(StringUtils.center("=", 50, "="));
		opsForSet.difference("city-group-3", Lists.newArrayList("city-group-1", "city-group-2"))
				.forEach(System.out::println);
		System.out.println(StringUtils.center("=", 50, "="));

		opsForSet.differenceAndStore("city-group-1", "city-group-3", "city-group-diff");

		opsForSet.members("city-group-diff").forEach(System.out::println);
		System.out.println(StringUtils.center("=", 50, "="));

		opsForSet.union("city-group-1", Lists.newArrayList("city-group-2", "city-group-3"))
				.forEach(System.out::println);
		System.out.println(StringUtils.center("=", 50, "="));

		opsForSet.unionAndStore("city-group-1", Lists.newArrayList("city-group-2", "city-group-3"), "city-group-union");
		opsForSet.members("city-group-union").forEach(System.out::println);
		System.out.println(opsForSet.isMember("city-group-union", "ha er bin"));

		System.out.println(StringUtils.center("=", 50, "="));
		Cursor<String> cursor = opsForSet.scan("city-group-union",
				ScanOptions.scanOptions().count(3).match("* *").build());

		System.out.println(opsForSet.size("city-group-union"));

		while (!cursor.isClosed() && cursor.hasNext()) {
			System.out.println("cursorId is : " + cursor.getCursorId());
			System.out.println("cursor position : " + cursor.getPosition());
			System.out.println(cursor.next());
		}

		System.out.println(StringUtils.center("=", 50, "="));
		System.out.println(opsForSet.pop("city-group-union", 3));

		System.out.println(StringUtils.center("=", 50, "="));
		String randomElement1 = opsForSet.randomMember("city-group-union");
		String randomElement2 = opsForSet.randomMember("city-group-union");
		System.out.println(randomElement1);
		System.out.println(randomElement2);

		/*
		 * randomMembers方法允许有重复，相当于srandmembers中count属性传入了负值，
		 * 因此在往randomMembers传count值时，只需要传正值即可。spring实际传入给redis时会传成负值。
		 */
		System.out.println(opsForSet.randomMembers("city-group-union", 5));
		System.out.println(opsForSet.randomMembers("city-group-union", 55));

		// 该方法相当于srandmembers中count属性值传入了正值。
		// 也就是说，想向srandmembers的count属性传负值，调randomMembers方法，想向srandmembers的count属性传正值，调distinctRandomMembers方法
		Set<String> randomMembers = opsForSet.distinctRandomMembers("city-group-union", 7);
		randomMembers.forEach(System.out::println);
		System.out.println(StringUtils.center("=", 50, "="));

		opsForSet.add("name-group-1", "zhang san", "li si", "tom", "jerry", "yu guo fei");
		opsForSet.add("name-group-2", "wang tian long", "yue qiang", "tom", "li song hao", "li si", "yue qiang",
				"wu ming");
		opsForSet.add("name-group-3", "li si", "yue qiang", "hu xun", "li song hao", "tom", "tony");
		opsForSet.add("name-group-4", "li si", "tom");
		opsForSet.add("name-group-5", "liu kai qian", "bai lu", "li si", "zhang san", "wang tian long", "li gang",
				"tom", "zhang hao", "jerry");

		Set<String> intersectGroup = opsForSet.intersect("name-group-1",
				Lists.newArrayList("name-group-2", "name-group-3", "name-group-4", "name-group-5"));
		intersectGroup.forEach(System.out::println);
		System.out.println(StringUtils.center("=", 50, "="));

		opsForSet.move("name-group-1", "zhang san", "name-group-2");
		opsForSet.move("name-group-1", "tom", "name-group-2");

		opsForSet.members("name-group-1").forEach(System.out::println);
		System.out.println();
		opsForSet.members("name-group-2").forEach(System.out::println);
		System.out.println(StringUtils.center("=", 50, "="));

		opsForSet.unionAndStore("name-group-1",
				Lists.newArrayList("name-group-2", "name-group-3", "name-group-4", "name-group-5"), "name-group-union");
		Cursor<String> cursor1 = opsForSet.scan("name-group-union",
				ScanOptions.scanOptions().count(5).match("* *").build());

		while (!cursor1.isClosed() && cursor1.hasNext()) {
			System.out.println("id is : " + cursor1.getCursorId());
			System.out.println("position is : " + cursor1.getPosition());
			System.out.println("contents is : " + cursor1.next());
		}

	}

	@Test
	@Ignore
	public void test10() {
		SortQuery<String> sortCondition = SortQueryBuilder.sort("cityList").by("hash_*->weight").get("hash_*->weight")
				.get("hash_*->code").get("#").alphabetical(false).limit(0, 2).order(Order.DESC).build();

		stringRedisTemplate.sort(sortCondition, "city-sorted");
		stringRedisTemplate.boundListOps("city-sorted").range(0, -1).forEach(System.out::println);

		SortQuery<String> sortCondition1 = SortQueryBuilder.sort("cityList").by("code_*").get("weight_*").get("code_*")
				.get("#").limit(1, 3).alphabetical(true).order(Order.ASC).build();
		System.out.println(stringRedisTemplate.sort(sortCondition1));
	}

	@Test
	@Ignore
	public void test11() {
		stringRedisTemplate.delete(stringRedisTemplate.keys("*"));

		ZSetOperations<String, String> opsForZSet = stringRedisTemplate.opsForZSet();
		opsForZSet.add("city-1", "tian jin", -90.123);
		opsForZSet.add("city-1", "he fei", -1.233391);
		opsForZSet.add("city-1", "tian tai", 71.2391);
		opsForZSet.add("city-1", "shi jia zhuang", 122.977);
		opsForZSet.add("city-1", "cheng de", 0.778);
		// opsForZSet.add("city-1", "hai nan", 9999);

		opsForZSet.incrementScore("city-1", "tian jin", 20);
		opsForZSet.incrementScore("city-1", "he fei", 90);
		opsForZSet.incrementScore("city-1", "hai nan", 9999);
		System.out.println(opsForZSet.score("city-1", "hai nan"));

		opsForZSet.score("city-union", "tian jin");

		opsForZSet.add("city-2", "tian jin", -100);
		opsForZSet.add("city-2", "shi jia zhuang", 58);
		opsForZSet.add("city-2", "hai nan", -170.88971);
		opsForZSet.add("city-2", "cheng de", 87728);
		opsForZSet.add("city-2", "shuang liu", -77.889);
		opsForZSet.add("city-2", "zhang jia kou", -55);
		opsForZSet.add("city-2", "huang shi", 123);

		opsForZSet.unionAndStore("city-1", "city-2", "city-union");

		Set<TypedTuple<String>> cityUnion = opsForZSet.rangeWithScores("city-union", 0, -1);
		for (TypedTuple<String> city : cityUnion) {
			System.out.println(city.getValue() + ":" + city.getScore());
		}

		System.out.println("=====================");

		opsForZSet.intersectAndStore("city-1", "city-2", "city-inter");

		Double score1 = opsForZSet.score("city-1", "hai nan");
		Double score2 = opsForZSet.score("city-2", "hai nan");
		System.out.println(score1);
		System.out.println(score2);

		Set<String> cityInter = opsForZSet.reverseRange("city-inter", 0, -1);
		for (String city : cityInter) {
			System.out.println(city + ":" + opsForZSet.score("city-inter", city));
		}

		System.out.println("=====================");

		Long deleted = opsForZSet.remove("city-union", "he fei", "shi jia zhuang", "huang shi", "bu cun zai",
				"mei guo");
		System.out.println("已删除" + deleted + "个元素");

		Set<String> cityUnion1 = opsForZSet.range("city-union", 0, -1);
		for (String city : cityUnion1) {
			System.out.println(city + ":" + opsForZSet.score("city-union", city));
		}
		deleted = opsForZSet.removeRangeByScore("city-union", -80, 100);
		System.out.println("已删除" + deleted + "个元素");

		Set<String> cityUnion2 = opsForZSet.range("city-union", 0, -1);
		for (String city : cityUnion2) {
			System.out.println(city + ":" + opsForZSet.score("city-union", city));
		}

		System.out.println(opsForZSet.rank("city-union", "cheng de"));
		System.out.println(opsForZSet.reverseRank("city-union", "cheng de"));

	}

	@Test
	@Ignore
	public void test12() {
		stringRedisTemplate.delete(stringRedisTemplate.keys("*"));

		HashOperations<String, Object, Object> opsForHash = stringRedisTemplate.opsForHash();

		opsForHash.put("base_dept", "dept_no", "4000");
		opsForHash.put("base_dept", "dept_code", "No:4000");
		opsForHash.put("base_dept", "dept_name", "4000Dept");

		Map<String, String> keyValueMap = new HashMap<>();
		keyValueMap.put("modify_date", "2017-11-30 21:53:22");
		keyValueMap.put("is_run", "0");
		keyValueMap.put("dept_level", "5");
		keyValueMap.put("parent_dept_no", "17");

		opsForHash.putAll("base_dept", keyValueMap);

		System.out.println(opsForHash.get("base_dept", "modify_date"));

		Map<Object, Object> deptMaps = opsForHash.entries("base_dept");
		deptMaps.forEach((key, value) -> System.out.println(key + ":" + value));

		opsForHash.increment("base_dept", "dept_no", -300);
		opsForHash.increment("base_dept", "salary", 3788.951123);

		System.out.println(opsForHash.get("base_dept", "dept_no"));
		System.out.println(opsForHash.get("base_dept", "salary"));

		System.out.println(opsForHash.hasKey("base_dept", "salary"));
		opsForHash.delete("base_dept", "salary");
		System.err.println(opsForHash.hasKey("base_dept", "salary"));

		opsForHash.putIfAbsent("base_dept", "x-code", "597176");
		opsForHash.putIfAbsent("base_dept", "dept_no", "6666");

		Set<Object> allKeys = opsForHash.keys("base_dept");
		for (Object key : allKeys) {
			System.out.println(key + ":" + opsForHash.get("base_dept", key));
		}

		List<Object> values = opsForHash.values("base_dept");
		System.out.println(values);

		List<Object> keys = new ArrayList<>();
		Iterator<Object> iterator = opsForHash.keys("base_dept").iterator();
		for (int index = 0; index < opsForHash.size("base_dept"); index++) {
			if (index > 5) {
				break;
			}
			keys.add(iterator.next());
		}

		List<Object> mValues = opsForHash.multiGet("base_dept", keys);
		System.out.println(keys);
		System.out.println(mValues);

	}

	@Test
	@Ignore
	public void test13() {
		stringRedisTemplate.delete(stringRedisTemplate.keys("*"));

		HyperLogLogOperations<String, String> opsForHyperLogLog = stringRedisTemplate.opsForHyperLogLog();
		opsForHyperLogLog.add("loginId1", "378", "399", "213", "377", "378", "999", "213", "3399", "999", "278");
		opsForHyperLogLog.add("loginId1", "399", "377", "400", "512", "77", "399", "213", "5555", "772", "5555");

		opsForHyperLogLog.add("loginId2", "775", "334", "373", "399", "775", "339", "555", "334", "775", "334");
		opsForHyperLogLog.add("loginId2", "1000", "512", "330", "775", "775", "512", "999", "337", "6666", "400");

		System.out.println(opsForHyperLogLog.size("loginId1")); // 12
		System.out.println(opsForHyperLogLog.size("loginId2")); // 13
		System.out.println(opsForHyperLogLog.size("loginId1", "loginId2")); // 21

		System.out.println(opsForHyperLogLog.union("loginId-union", "loginId1", "loginId2")); // 21
		assert opsForHyperLogLog.size("loginId-union") == opsForHyperLogLog.size("loginId1", "loginId2");

	}

	@Test
	@Ignore
	public void test14() {
		stringRedisTemplate.execute((RedisConnection conn) -> {
			HashMap<String, String> map = new HashMap<>();
			map.put("hello", "world");
			byte[] serialize = redisSerializer.serialize(map);

			// conn.publish("test".getBytes(), serialize);
			conn.publish("test".getBytes(), "hello world".getBytes());

			return null;
		});

		try {
			Thread.sleep(TimeUnit.MINUTES.toMillis(2));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void test15() {
		File objectFile = new File("/Users/maziqiang/Downloads/dept.txt");

		BaseDept dept = new BaseDept(30, "No.30", "30公司");
		dept.setModifyDate(new Date());
		dept.setIsRun(false);

		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(objectFile))) {
			output.writeObject(dept);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test16() {
		File objectFile = new File("/Users/maziqiang/Downloads/dept.txt");

		try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(objectFile))) {
			Object deserialized = input.readObject();
			if (BaseDept.class.isInstance(deserialized)) {
				BaseDept dept = BaseDept.class.cast(deserialized);
				System.out.println(dept.getDeptName());
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test17() {
		BaseDept dept = new BaseDept(30, "No.30", "30公司");
		dept.setModifyDate(new Date());
		dept.setIsRun(false);

		// 使用SerializationUtils类，可以免去直接操作ObjectOutputStream，直接获取dept对象序列化后的数据内容
		byte[] objectData = SerializationUtils.serialize(dept);

		// 使用SerializationUtils类，可以免去直接操作ObjectInputStream，只需要给出序列化后的数据内容，即可进行反序列化，获得对应的对象
		Object deserialized = SerializationUtils.deserialize(objectData);

		BaseDept deptCopy = (BaseDept) deserialized;
		System.out.println(deptCopy.getDeptName());
	}

	@Test
	@Ignore
	public void test18() {
		BaseDept dept = new BaseDept(30, "No.30", "30公司");
		dept.setModifyDate(new Date());
		dept.setIsRun(false);

		/*
		 * JdkSerializationRedisSerializer这个类只是spring-
		 * redis包为了对redis的key和value进行序列化和反序列化操作才提出来的，不应作为一个平时进行序列化的工具来使用。
		 * 
		 * 平时如果需要进行序列化和反序列化的操作，除了使用jdk自带的ObjectOutputStream和ObjectInputStream，
		 * 还可以使用更简便的apache commons-lang包下的SerializationUtils类。
		 */
		JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer();
		// 直接调用serialize方法，即可获得BaseDept对象序列化后的二进制内容
		byte[] objectData = jdkSerializer.serialize(dept);

		// 调用deserialize方法，给出序列化后的内容，即可进行反序列化
		Object deserialized = jdkSerializer.deserialize(objectData);

		BaseDept deptCopy = (BaseDept) deserialized;
		System.out.println(deptCopy.getDeptName());
	}

	@Test
	public void test19() {
		// 1.最原始的使用RedisConnection的方式，直接从RedisConnectionFactory中获取RedisConnection对象。缺点很明显，需要手动关闭Redis连接。
		RedisConnection connection = redisConnection.getConnection();
		connection.publish("test".getBytes(), "hello world".getBytes());
		connection.publish("test".getBytes(), SerializationUtils.serialize(new Date()));
		connection.close();

		// 2.使用RedisTemplate，利用execute方法，只需要给出使用RedisConnection的回调处理即可，无需进行RedisConnection的关闭操作。
		stringRedisTemplate.execute((RedisConnection conn) -> {
			StringRedisConnection strConnection = (StringRedisConnection) conn;
			/*
			 * 利用StringRedisConnection，这样就可以直接传字符串，
			 * 由StringRedisConnection来进行字符串的序列化
			 * 
			 * 注意：RedisConnection是底层的API，它负责和redis服务器进行交互，它在交互时使用底层的byte[]来进行交互。
			 * 而StringRedisConnection是对RedisConnection的扩展，它提供了直接给出String类型的参数的方法
			 * ，实际上的处理是把传入的String对象转换成二进制byte[]（通过String.getBytes()方法），
			 * 然后再传入给RedisConnection的底层API。这就方便了仅使用String对象作为redis的key和value时，
			 * 直接使用String对象，而不用再手动转换成byte[]了。
			 */
			strConnection.publish("test", "hello world");
			strConnection.publish("test".getBytes(), SerializationUtils.serialize(new Date()));

			return null;
		});

		/*
		 * convertAndSend方法会使用RedisTemplate的stringRedisSerializer来对传入的String类型的channel进行序列化
		 * 同时会使用RedisTemplate的ValueSerializer来对传入的Object类型的message进行序列化
		 */
		redisTemplate.convertAndSend("test", new Date());
		/*
		 * 如果RedisTemplate的ValueSerializer是null 并且 message本身就是byte[]类型，
		 * 那么就不用对message进行序列化。
		 */
		redisTemplate.convertAndSend("test", new byte[] { 32, 57, 89, 122, 77, 31 });
		redisTemplate.convertAndSend("hello", new byte[] { 32, 57, 89, 122, 77, 31 });
	}
}