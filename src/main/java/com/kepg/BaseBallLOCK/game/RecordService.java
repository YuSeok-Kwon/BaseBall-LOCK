package com.kepg.BaseBallLOCK.game;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.game.record.domain.BatterRecord;
import com.kepg.BaseBallLOCK.game.record.domain.PitcherRecord;
import com.kepg.BaseBallLOCK.game.record.repository.BatterRecordRepository;
import com.kepg.BaseBallLOCK.game.record.repository.PitcherRecordRepository;
import com.kepg.BaseBallLOCK.player.domain.Player;
import com.kepg.BaseBallLOCK.player.service.PlayerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final BatterRecordRepository batterRecordRepository;
    private final PitcherRecordRepository pitcherRecordRepository;
    private final PlayerService playerService;

    public void saveBatterRecord(int scheduleId, int teamId, int pa, int ab, int hits, int hr, int rbi, int bb, int so, int sb, String playerName) {
        Optional<Player> player = playerService.findByNameAndTeamId(playerName, teamId);
        if (player.isEmpty()) return;

        BatterRecord record = BatterRecord.builder()
                .scheduleId(scheduleId)
                .teamId(teamId)
                .playerId(player.get().getId())
                .pa(pa)
                .ab(ab)
                .hits(hits)
                .hr(hr)
                .rbi(rbi)
                .bb(bb)
                .so(so)
                .sb(sb)
                .build();

        batterRecordRepository.save(record);
    }

    public void savePitcherRecord(int scheduleId, int teamId, String playerName, double innings, int strikeouts, int bb, int hbp, int runs, int er, int hits, int hr, String decision) {
        Optional<Player> player = playerService.findByNameAndTeamId(playerName, teamId);
        if (player.isEmpty()) return;

        PitcherRecord record = PitcherRecord.builder()
                .scheduleId(scheduleId)
                .teamId(teamId)
                .playerId(player.get().getId())
                .innings(innings)
                .strikeouts(strikeouts)
                .bb(bb)
                .hbp(hbp)
                .runs(runs)
                .earnedRuns(er)
                .hits(hits)
                .hr(hr)
                .decision(decision)
                .build();

        pitcherRecordRepository.save(record);
    }
}
