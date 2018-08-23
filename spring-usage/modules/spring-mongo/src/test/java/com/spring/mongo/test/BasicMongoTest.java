package com.spring.mongo.test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoClientOptionsFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.WriteConcern;
import com.spring.redis.data.domain.BaseDept;
import com.spring.redis.data.domain.BaseLine;
import com.spring.redis.data.domain.DomainUtils;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class BasicMongoTest {

	@Configuration
	static class Config {

		@Bean
		public MongoClientOptionsFactoryBean mongoClientOptions() {
			MongoClientOptionsFactoryBean mongoClientOptions = new MongoClientOptionsFactoryBean();
			mongoClientOptions.setMinConnectionsPerHost(5);
			mongoClientOptions.setConnectionsPerHost(20);
			mongoClientOptions.setMaxConnectionIdleTime((int) TimeUnit.SECONDS.toMillis(90));
			mongoClientOptions.setMaxConnectionLifeTime((int) TimeUnit.MINUTES.toMillis(5));
			mongoClientOptions.setMaxWaitTime((int) TimeUnit.SECONDS.toMillis(10));
			mongoClientOptions.setConnectTimeout((int) TimeUnit.MINUTES.toMillis(2));

			return mongoClientOptions;
		}

		@Bean
		public MongoClientFactoryBean mongoClient(MongoClientOptions mongoClientOptions) {
			MongoClientFactoryBean mongoClient = new MongoClientFactoryBean();
			mongoClient.setHost("localhost");
			mongoClient.setPort(27017);
			mongoClient.setMongoClientOptions(mongoClientOptions);

			return mongoClient;
		}

		@Bean
		public MongoDbFactory dbFactory(MongoClient mongoClient) {
			MongoDbFactory dbFactory = new SimpleMongoDbFactory(mongoClient, "helloWorld");

			return dbFactory;
		}

		@Bean
		public MongoTemplate mongoTemplate(MongoDbFactory dbFactory) {
			MongoTemplate mongoTemplate = new MongoTemplate(dbFactory);
			mongoTemplate.setWriteConcern(WriteConcern.UNACKNOWLEDGED);

			return mongoTemplate;
		}
	}

	static class Person {

		private String id;
		private String name;
		private int age;

		public Person(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

		@Override
		public String toString() {
			return "Person [id=" + id + ", name=" + name + ", age=" + age + "]";
		}

	}

	@Autowired
	private MongoTemplate mongoTemplate;

	@Test
	public void test1() {
		Person person = new Person("xiaoming", RandomUtils.nextInt());
		mongoTemplate.insert(person);

		List<Person> allPerson = mongoTemplate.findAll(Person.class);
		System.out.println(allPerson);
	}

	@Test
	public void test2() {
		BaseLine line = DomainUtils.generateLine(RandomUtils.nextInt(100, 900), 91);
		mongoTemplate.insert(line);

		BaseLine lineFromMongo = mongoTemplate.findById(line.getLineNo(), BaseLine.class);
		System.out.println(lineFromMongo.getLineName() + ":" + lineFromMongo.getDept().getDeptName());
	}

	@Test
	public void test3() {
		BaseDept dept = DomainUtils.generateDept(RandomUtils.nextInt(1, 100));
		mongoTemplate.insert(dept, "helloWorld");

		BaseDept deptFromMongo = mongoTemplate.findById(dept.getDeptNo(), BaseDept.class, "helloWorld");
		System.out.println(deptFromMongo.getDeptName());
	}
}
