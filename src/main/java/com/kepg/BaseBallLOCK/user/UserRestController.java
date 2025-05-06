package com.kepg.BaseBallLOCK.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kepg.BaseBallLOCK.simulationGame.card.userCard.domain.UserCard;
import com.kepg.BaseBallLOCK.simulationGame.card.userCard.dto.UserCardViewDTO;
import com.kepg.BaseBallLOCK.simulationGame.card.userCard.service.UserCardService;
import com.kepg.BaseBallLOCK.user.userDomain.User;
import com.kepg.BaseBallLOCK.user.userService.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserRestController {
	
	private final UserService userService;
	private final UserCardService userCardService;


	// 로그인 API 
	@PostMapping("/login")
	public Map<String, String> login(
			@RequestParam String loginId
			, @RequestParam String password
			, HttpSession session) {
		
		User user = userService.getUser(loginId, password);
		
		Map<String, String> resultMap = new HashMap<>();
		
		if(user != null) {
			session.setAttribute("loginUser", user);
			
			session.setAttribute("userId", user.getId());
			session.setAttribute("userName", user.getName());
			session.setAttribute("userNickname", user.getNickname());
			session.setAttribute("favoriteTeamId", user.getFavoriteTeamId());

			
			resultMap.put("result", "success");
		} else {
			// 실패
			resultMap.put("result", "fail");
		}
		
		return resultMap;	
	}
	
	// 회원가입 API
	@PostMapping("/join")
	public Map<String, String> join(
			@RequestParam String loginId
			, @RequestParam String password
			, @RequestParam String name
			, @RequestParam String email
			, @RequestParam String nickname
			, @RequestParam int favoriteTeamId) {
		Map<String, String> resultMap = new HashMap<>();
		
		if(userService.addUser(loginId, password, name, email, nickname, favoriteTeamId)) {
			resultMap.put("result", "success");
		} else {
			resultMap.put("result", "fail");
		}
		return resultMap;
	}
	
	// Id 중복확인 API
	@GetMapping("/duplicate-id")
	public Map<String, Boolean> isDuplicateId(
			@RequestParam String loginId){
		
		Map<String, Boolean> resultMap = new HashMap<>();
		
		if(userService.duplicateId(loginId)) {
			resultMap.put("result", true);
		} else {
			resultMap.put("result", false);
		}
		return resultMap;
	}
	
	// 닉네임 중복확인 API
	@GetMapping("/duplicate-nickname")
	public Map<String, Boolean> isDuplicateNickname(
			@RequestParam String nickname){
		
		Map<String, Boolean> resultMap = new HashMap<>();
		
		if(userService.duplicateNickname(nickname)) {
			resultMap.put("result", true);
		} else {
			resultMap.put("result", false);
		}
		return resultMap;
	}
	
	// 아이디 찾기 API
		@PostMapping("/findId")
		public Map<String, String> findId(
				@RequestParam String name
				, @RequestParam String email){
			
			Map<String, String> resultMap = new HashMap<>();
			
			User user = userService.findLoginId(name, email);
			
			if(user != null && user.getLoginId() != null) {
				resultMap.put("result", "success");
				resultMap.put("id", user.getLoginId());
			} else {
				resultMap.put("result", "not found");
				resultMap.put("id", "null");
			}
			return resultMap;
		}
		
		// 비밀번호 재설정 위한 정보조회 API
		@PostMapping("/findPassword")
		public Map<String, String> checkUser (
				@RequestParam String loginId
				, @RequestParam String name
				, @RequestParam String email){
			
			Map<String, String> resultMap = new HashMap<>();
			
			if(userService.findUser(loginId, name, email)) {
				resultMap.put("result", "true");
				resultMap.put("loginId", loginId);
			} else {
				resultMap.put("result", "false");
			}
			return resultMap;
		}
		
		// 비밀번호 재설정 API
		@PostMapping("/resetPassword")
		public Map<String, String> resetPassword(
				@RequestParam String loginId
				, @RequestParam String password){
			
			Map<String, String> resultMap = new HashMap<>();
			
			if(userService.resetPassword(loginId, password)) {
				resultMap.put("result", "success");
			} else {
				resultMap.put("result", "fail");
			}
			return resultMap;
			}
				
		@GetMapping("/my-cards")
		public List<UserCard> getMyCards(HttpSession session) {
		    Integer userId = (Integer) session.getAttribute("userId"); 

		    if (userId == null) {
		        throw new IllegalStateException("로그인된 사용자 정보가 없습니다.");
		    }

		    return userCardService.findByUserId(userId);
		}
		
		@GetMapping("/cards")
		public List<UserCardViewDTO> getCardList(HttpSession session) {
		    Integer userId = (Integer) session.getAttribute("userId");
		    return userCardService.getUserCardViewList(userId);
		}
//		// 정보 수정 API
//		@PostMapping("/addInfo")
//		public Map<String, String> addInfo(
//				@ModelAttribute UserRequestDTO userRequestDto
//				, HttpSession session) {
//			
//			int userId = ((Long)session.getAttribute("userId")).intValue();
//			
//		    MultipartFile profileImage = userRequestDto.getProfileImage();
//		    String profileImagePath = null;
//
//		    if (profileImage != null && !profileImage.isEmpty()) {
//		    	profileImagePath = FileManager.saveFile(userId, profileImage);
//		    }
//			
//			String introduce = userRequestDto.getIntroduce();
//			String gender = userRequestDto.getGender();
//			String phoneNumber = userRequestDto.getPhoneNumber();
//			String birth = userRequestDto.getBirth();
//			
//			Map<String, String> resultMap = new HashMap<>();
//			if(userService.addUserInfo(userId, introduce, gender, phoneNumber, birth, profileImagePath)) {
//				resultMap.put("result", "success");
//			} else {
//				resultMap.put("result", "fail");
//			}
//			return resultMap;
//		}
}