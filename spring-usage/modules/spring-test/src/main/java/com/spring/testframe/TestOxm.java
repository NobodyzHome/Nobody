package com.spring.testframe;

import java.io.IOException;
import java.util.Date;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.spring.data.domain.Student;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/test/containers/oxm.xml")
public class TestOxm {

	@Autowired
	private Jaxb2Marshaller marshaller;

	@Value("file:test.xml")
	private Resource resource;

	@Test
	public void test1() throws IOException {
		Student student = new Student();
		student.setId(100);
		student.setName("小明");
		student.setSex("男");
		student.setBirthDay1(new Date());
		
		Student student2 = new Student();
		student2.setId(300);
		student2.setName("小黄");
		student2.setSex("男");
		
		student.setFriend(student2);

		marshaller.marshal(student, new StreamResult(resource.getFile()));

		Student student1 = (Student)marshaller.unmarshal(new StreamSource(resource.getFile()));
		System.out.println(student1.getName());
	}
}
