package com.spring.data.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spring.data.domain.BaseDept;

@Repository
public class BaseDeptHibernateDao {

	@Autowired
	private SessionFactory sessionFactory;

	public BaseDept findById(Integer deptNo) {
		Session session = sessionFactory.getCurrentSession();
		BaseDept dept = session.get(BaseDept.class, deptNo);

		return dept;
	}

}
