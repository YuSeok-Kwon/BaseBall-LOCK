package com.kepg.BaseBallLOCK.simulationGame.card.userCard;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kepg.BaseBallLOCK.simulationGame.card.userCard.dto.UserCardViewDTO;
import com.kepg.BaseBallLOCK.simulationGame.card.userCard.service.UserCardService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequestMapping("/game")
@Controller
@RequiredArgsConstructor
public class UserCardController {
	
	private final UserCardService userCardService;
	
    @GetMapping("/cards-view")
    public String viewMyCards(Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        List<UserCardViewDTO> cards = userCardService.getUserCardViewList(userId);
        model.addAttribute("cards", cards);
        
        return "user/my-cards"; // 연결 경로
    }
    
    @DeleteMapping("/card/delete")
    @ResponseBody
    public ResponseEntity<Void> deleteCard(@RequestParam Integer playerId,
                                           @RequestParam Integer season,
                                           HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        userCardService.deleteCard(userId, playerId, season);
        return ResponseEntity.ok().build();
    }
}
