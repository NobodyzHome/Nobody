package com.spring.cache.jdk;

import java.text.DateFormat;
import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.validator.constraints.Range;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.spring.data.domain.BaseDept;

@Service
@Validated
public class ConcurrentMapCacheService {

	public BaseDept cacheAbleService(BaseDept dept, String words, Date date) {
		BaseDept dept1 = new BaseDept();
		dept1.setDeptNo(dept.getDeptNo());
		dept1.setDeptName(dept.getDeptName());
		dept1.setIsRun(dept.getIsRun());
		dept1.setDeptCode(words);
		dept1.setModifyDate(date);

		return dept1;
	}

	public String cacheAbleService2(@NotNull @Past Date date) {
		String formatted = DateFormat.getDateTimeInstance().format(date);
		return formatted;
	}

	public void clearSpecificCache(@Range(min = 1, max = 2000) int deptNo) {
		System.out.println("ConcurrentMapCacheService.clearSpecificCache()");
	}

	public void clearAllCache() {
		System.out.println("ConcurrentMapCacheService.clearAllCache()");
	}

	public BaseDept clearThenPutCache(BaseDept dept) {
		BaseDept deptCloned = SerializationUtils.clone(dept);
		return deptCloned;
	}

	public BaseDept forceUpdateCache(Integer deptNo, String deptCode, String deptName, Date modifyDate, Boolean isRun) {
		BaseDept dept = new BaseDept();
		dept.setDeptNo(deptNo);
		dept.setDeptCode(deptCode);
		dept.setDeptName(deptName);
		dept.setModifyDate(modifyDate);
		dept.setIsRun(isRun);

		return dept;
	}

}
