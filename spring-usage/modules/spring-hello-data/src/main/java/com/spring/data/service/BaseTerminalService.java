package com.spring.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.data.dao.hibernate.BaseTerminalHibernateDao;
import com.spring.data.dao.mapper.BaseTerminalMapper;
import com.spring.data.domain.BaseTerminal;

@Service
@Transactional("hibernateTransactionManager")
public class BaseTerminalService {

	@Autowired
	private BaseTerminalHibernateDao terminalDao;

	@Autowired
	private BaseTerminalMapper terminalMapper;

	@Transactional(readOnly = true)
	public BaseTerminal queryTerminal(Integer terminalNo) {
		BaseTerminal terminal = terminalDao.queryTerminal(terminalNo);
		return terminal;
	}

	public void saveOrUpdateTerminal(BaseTerminal example) {
		terminalDao.saveOrUpdate(example);
	}

	public void deleteTerminal(Integer terminalNo) {
		terminalDao.delete(new BaseTerminal(terminalNo));
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public BaseTerminal queryTerminalMybatis(Integer terminalNo) {
		BaseTerminal terminal = terminalMapper.findByIdWithLines(terminalNo);
		return terminal;
	}
	
}