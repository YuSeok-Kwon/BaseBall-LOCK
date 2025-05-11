package com.kepg.BaseBallLOCK.simulationGame.event;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kepg.BaseBallLOCK.simulationGame.event.answer.domain.GameEventAnswer;
import com.kepg.BaseBallLOCK.simulationGame.event.question.domain.GameEventQuestion;
import com.kepg.BaseBallLOCK.simulationGame.event.service.GameEventService;
import com.kepg.BaseBallLOCK.simulationGame.result.dto.GameResultRequestDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game/game-event")
public class GameEventRestController {

    private final GameEventService gameEventService;

    @GetMapping("/questions")
    public List<GameEventQuestion> getQuestions() {
        return gameEventService.getRandomFiveQuestions();
    }

    @PostMapping("/answer")
    public ResponseEntity<?> saveAnswer(@RequestBody GameEventAnswer answer) {
        gameEventService.saveUserAnswer(
            answer.getScheduleId(), answer.getQuestionId(), answer.getAnswerText()
        );
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/result")
    public ResponseEntity<?> getResult(@RequestParam int scheduleId) {
        boolean isWin = gameEventService.evaluateGameResult(scheduleId);
        return ResponseEntity.ok().body(Map.of("isWin", isWin));
    }
    
    @PostMapping("/submit")
    public ResponseEntity<?> submitResult(@RequestBody GameResultRequestDTO request) {
        List<GameEventAnswer> answers = gameEventService.getAnswersByScheduleId(request.getScheduleId(), request.getUserId());
        gameEventService.evaluateAndSaveResult(request.getScheduleId(), request.getUserId(), answers);
        return ResponseEntity.ok().build();
    }
}
