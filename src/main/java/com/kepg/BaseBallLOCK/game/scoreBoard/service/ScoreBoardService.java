package com.kepg.BaseBallLOCK.game.scoreBoard.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kepg.BaseBallLOCK.game.scoreBoard.domain.ScoreBoard;
import com.kepg.BaseBallLOCK.game.scoreBoard.repository.ScoreBoardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScoreBoardService {

    private final ScoreBoardRepository scoreBoardRepository;

    @Transactional
    public void saveOrUpdate(ScoreBoard scoreBoard) {
        ScoreBoard existing = scoreBoardRepository.findByScheduleId(scoreBoard.getScheduleId());

        if (existing != null) {
            ScoreBoard updated = ScoreBoard.builder()
                .id(existing.getId())
                .scheduleId(scoreBoard.getScheduleId())
                .homeScore(scoreBoard.getHomeScore())
                .awayScore(scoreBoard.getAwayScore())
                .winPitcher(scoreBoard.getWinPitcher())
                .losePitcher(scoreBoard.getLosePitcher())
                .homeInningScores(scoreBoard.getHomeInningScores())
                .awayInningScores(scoreBoard.getAwayInningScores())
                .homeR(scoreBoard.getHomeR())
                .homeH(scoreBoard.getHomeH())
                .homeE(scoreBoard.getHomeE())
                .homeB(scoreBoard.getHomeB())
                .awayR(scoreBoard.getAwayR())
                .awayH(scoreBoard.getAwayH())
                .awayE(scoreBoard.getAwayE())
                .awayB(scoreBoard.getAwayB())
                .build();

            scoreBoardRepository.save(updated);
        } else {
            scoreBoardRepository.save(scoreBoard);
        }
    }
    
    public ScoreBoard findByScheduleId(int scheduleId) {
        return scoreBoardRepository.findByScheduleId(scheduleId);
    }
}
