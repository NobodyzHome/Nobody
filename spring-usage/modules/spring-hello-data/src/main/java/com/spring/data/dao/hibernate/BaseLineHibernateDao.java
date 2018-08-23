package com.spring.data.dao.hibernate;

import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spring.data.domain.BaseLine;

@Repository
public class BaseLineHibernateDao {

	@Autowired
	private SessionFactory sessionFactory;

	public BaseLine queryById(Integer lineNo) {
		Session session = sessionFactory.getCurrentSession();
		BaseLine line = session.get(BaseLine.class, lineNo);

		return line;
	}

	public void deleteById(Integer lineNo) {
		Session session = sessionFactory.getCurrentSession();
		Optional.ofNullable(queryById(lineNo)).ifPresent(session::delete);
	}
}
