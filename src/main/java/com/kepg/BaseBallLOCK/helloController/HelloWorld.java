package com.kepg.BaseBallLOCK.helloController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class HelloWorld {
	
	@ResponseBody
	@RequestMapping("/baseballLOCK/hello")
	public String hello() {
		return "hello BaseBall";
	}
}
