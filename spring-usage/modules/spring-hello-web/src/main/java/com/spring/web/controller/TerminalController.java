package com.spring.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.data.domain.BaseTerminal;
import com.spring.data.service.BaseTerminalService;

@Controller
@RequestMapping("/terminal")
public class TerminalController {

	@Autowired
	private BaseTerminalService terminalService;

	@RequestMapping("/query/hibernate/{terminalNo}")
	@ResponseBody
	public BaseTerminal queryTerminalHibernate(@PathVariable Integer terminalNo) {
		BaseTerminal terminal = terminalService.queryTerminal(terminalNo);
		return terminal;
	}

	@RequestMapping("/query/mybatis/{terminalNo}")
	@ResponseBody
	public BaseTerminal queryTerminalMybatis(@PathVariable Integer terminalNo) {
		BaseTerminal terminal = terminalService.queryTerminalMybatis(terminalNo);
		return terminal;
	}

	@GetMapping("/show/{terminalNo}")
	public String showTerminal(@PathVariable Integer terminalNo, Model model) {
		BaseTerminal terminal = terminalService.queryTerminal(terminalNo);
		terminal = terminal == null ? new BaseTerminal() : terminal;
		model.addAttribute(terminal);

		return "show/terminal";
	}

	@PostMapping("/saveOrUpdate")
	public String saveOrUpdateTerminal(BaseTerminal terminal) {
		terminalService.saveOrUpdateTerminal(terminal);
		return "redirect:show/" + terminal.getTerminalNo();
	}

	@GetMapping("/delete/{terminalNo}")
	public String deleteTerminal(@PathVariable Integer terminalNo) {
		terminalService.deleteTerminal(terminalNo);
		/*
		 * 如果forward:或redirect:中给出的路径是相对路径，那么相对的则是当前请求的父路径。那么在给出的相对路径中，
		 * 可以继续给出"../"的样式来向上寻找路径。
		 * 
		 * 例如当前请求的路径是/terminal/delete/126，我们现在想访问/terminal/show/126，应该如何给出相对路径呢？
		 * 
		 * 1.首先由于需要使用相对路径，那么找到相对于哪个路径，即请求的父路径，也就是/terminal/delete。
		 * 2.然后由于还需要向上跳转一次路径，才能到/terminal路经，因此需要给出“../”继续向上跳转。
		 * 3.最后给出/terminal后的其余路径，也就是../show/126
		 */
		return "forward:../show/" + terminalNo;
	}
}