package com.spring.data.dao.hibernate;

import java.util.Objects;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spring.data.domain.BaseTerminal;

@Repository
public class BaseTerminalHibernateDao {

	@Autowired
	private SessionFactory sessionFactory;

	public BaseTerminal queryTerminal(Integer terminalNo) {
		Session currentSession = sessionFactory.getCurrentSession();
		BaseTerminal terminal = currentSession.get(BaseTerminal.class, terminalNo);
		return terminal;
	}

	public void saveOrUpdate(BaseTerminal terminal) {
		Session session = sessionFactory.getCurrentSession();
		BaseTerminal terminalSearched = null;

		if (Objects.nonNull(terminal.getTerminalNo())
				&& Objects.nonNull(terminalSearched = queryTerminal(terminal.getTerminalNo()))) {
			BeanUtils.copyProperties(terminal, terminalSearched);
		} else {
			session.save(terminal);
		}
	}

	public void delete(BaseTerminal terminal) {
		Session session = sessionFactory.getCurrentSession();
		Optional.ofNullable(queryTerminal(terminal.getTerminalNo())).ifPresent(session::delete);
	}
}
