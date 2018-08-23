package com.spring.redis.data.domain;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;

public class DomainUtils {
	private DomainUtils() {
	}

	public static BaseDept generateDept(int deptNo) {
		BaseDept dept = new BaseDept(deptNo, "No:" + deptNo, deptNo + "公司");
		dept.setIsRun(RandomUtils.nextBoolean());
		dept.setModifyDate(DateUtils.addDays(new Date(), -deptNo));

		return dept;
	}

	public static BaseDept generateDept() {
		int deptNo = RandomUtils.nextInt(10, 100);

		return generateDept(deptNo);
	}

	public static BaseDept[] generateDepts(int counts) {
		BaseDept[] depts = new BaseDept[counts];
		for (int index = 0; index < counts; index++) {
			depts[index] = generateDept();
		}

		return depts;
	}

	public static BaseDept[] generateDepts(int... deptNoArray) {
		BaseDept[] depts = new BaseDept[deptNoArray.length];
		AtomicInteger index = new AtomicInteger(0);
		Arrays.stream(deptNoArray).forEach(deptNo -> {
			depts[index.getAndAdd(1)] = generateDept(deptNo);
		});

		return depts;
	}

	public static BaseLine generateLine(int lineNo, int deptNo) {
		BaseLine line = new BaseLine(lineNo, "#" + lineNo, lineNo + "线路");
		line.setIsRun(RandomUtils.nextBoolean());
		line.setModifyDate(DateUtils.addDays(new Date(), lineNo));
		line.setDept(generateDept(deptNo));

		return line;
	}

	public static BaseLine generateLine() {
		int lineNo = RandomUtils.nextInt(200, 300);
		int deptNo = RandomUtils.nextInt(10, 100);

		return generateLine(lineNo, deptNo);
	}

	public static BaseLine[] generateLines(int... idArray) {
		if (idArray.length % 2 != 0) {
			return null;
		} else {
			BaseLine[] lineArray = new BaseLine[idArray.length / 2];
			int lineIndex = 0;

			for (int index = 0;;) {
				int lineNo = idArray[index++];
				int deptNo = idArray[index++];

				BaseLine line = generateLine(lineNo, deptNo);
				lineArray[lineIndex++] = line;

				if (index >= idArray.length) {
					break;
				}
			}

			return lineArray;
		}
	}

	public static BaseLine[] generateLines(int counts) {
		BaseLine[] lines = new BaseLine[counts];
		for (int index = 0; index < counts; index++) {
			lines[index] = generateLine();
		}

		return lines;
	}

}
