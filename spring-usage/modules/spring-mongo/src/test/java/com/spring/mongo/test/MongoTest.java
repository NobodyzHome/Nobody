package com.spring.mongo.test;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Sets;
import com.spring.data.domain.Student;

import static com.spring.data.domain.generator.StudentGenerator.students;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class MongoTest {

	@Autowired
	private MongoTemplate template;

	@Test
	public void test1() {
		Student[] students = students(10);
		template.insertAll(Sets.newHashSet(students));
	}

	@Test
	public void test2() {
		Student student = template.findOne(Query.query(Criteria.where("_id").lt(100)), Student.class);
		System.out.println(student.getName());
		
	}
}
