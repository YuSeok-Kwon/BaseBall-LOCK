package com.kepg.BaseBallLOCK.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/user")
@Controller
public class UserController {

	@GetMapping("/login-view")
	public String loginView() {
		return "user/login";
	}
	
	@GetMapping("/join-view")
	public String joinView() {
		return "user/join";
	}
	
	@GetMapping("/find-id-view")
	public String findIdView() {
		return "user/findId";
	}
	
	@GetMapping("/find-password-view")
	public String findPasswordView() {
		return "user/findPassword";
	}
	
	@GetMapping("/home")
	public String homeView() {
		return "user/home";
	}
}
