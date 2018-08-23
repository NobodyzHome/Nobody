package com.spring.data.domain.generator;

import static org.apache.commons.lang3.RandomUtils.nextInt;

import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.spring.data.domain.Student;

public class StudentGenerator {

	private static final String[] NAMES = { "小明", "小花", "小王", "小赵", "张三", "李四", "王五", "赵六", "小刚" };
	private static final String[] SEXES = { "男", "女" };
	private static final String[] INTERESTS = { "唱歌", "跳舞", "看书", "跑步", "游泳", "旅行", "美食", "编程" };

	public static Student student() {
		int id = nextInt(0, 1000);
		String name = NAMES[nextInt(0, NAMES.length)];
		String sex = SEXES[nextInt(0, SEXES.length)];
		String[] interests = ArrayUtils.subarray(INTERESTS, 0, nextInt(1, INTERESTS.length));
		Date birthDay = DateUtils.addDays(new Date(), id);

		Student student = new Student(id, name);
		student.setSex(sex);
		student.setInterest(interests);
		student.setBirthDay(birthDay);

		return student;
	}

	public static Student studentWithFriend() {
		Student student = student();
		Student friend = student();

		student.setFriend(friend);

		return student;
	}

	public static Student[] students(int counts) {
		Student[] students = new Student[counts];

		for (int index = 0; index < counts; index++) {
			Student student = studentWithFriend();
			students[index] = student;
		}

		return students;
	}

}
