package com.spring.event.listener;

import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class ContextListener {

	@EventListener
	public void onContextRefresh(ContextRefreshedEvent event) {
		System.out.println("ContextListener.onContextRefresh()");
	}

	@EventListener
	public void onContextClose(ContextClosedEvent event) {
		System.out.println("ContextListener.onContextClose()");
	}
}
