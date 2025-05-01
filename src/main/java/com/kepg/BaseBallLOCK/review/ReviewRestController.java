package com.kepg.BaseBallLOCK.review;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kepg.BaseBallLOCK.review.dto.ReviewDTO;
import com.kepg.BaseBallLOCK.review.service.ReviewService;
import com.kepg.BaseBallLOCK.user.userDomain.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/review")
@RestController
public class ReviewRestController {
	
    private final ReviewService reviewService;

	
	@PostMapping("/save")
	public ResponseEntity<Map<String, Object>> saveReview(@RequestBody ReviewDTO reviewDTO, HttpSession session) {
	    Map<String, Object> result = new HashMap<>();

	    User user = (User) session.getAttribute("loginUser");
		
	    if (user == null) {
	        result.put("success", false);
	        result.put("message", "로그인이 필요합니다.");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
	    }

	    try {
	        reviewService.saveReview(user.getId(), reviewDTO);
	        result.put("success", true);
	        result.put("message", "리뷰 저장 성공");
	        return ResponseEntity.ok(result);
	    } catch (Exception e) {
	        result.put("success", false);
	        result.put("message", "리뷰 저장 중 오류가 발생했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
	    }
	}
}
