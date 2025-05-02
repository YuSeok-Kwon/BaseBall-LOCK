package com.kepg.BaseBallLOCK.game.record.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.game.record.domain.BatterRecord;
import com.kepg.BaseBallLOCK.game.record.domain.PitcherRecord;
import com.kepg.BaseBallLOCK.game.record.dto.BatterRecordDTO;
import com.kepg.BaseBallLOCK.game.record.dto.PitcherRecordDTO;
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
        if (player.isEmpty()) {
            return;
        }

        int playerId = player.get().getId();

        boolean exists = batterRecordRepository.existsByScheduleIdAndTeamIdAndPlayerId(scheduleId, teamId, playerId);
        if (exists) {
            return;
        }

        BatterRecord record = BatterRecord.builder()
                .scheduleId(scheduleId)
                .teamId(teamId)
                .playerId(playerId)
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

        int playerId = player.get().getId();

        boolean exists = pitcherRecordRepository.existsByScheduleIdAndTeamIdAndPlayerId(scheduleId, teamId, playerId);
        if (exists) return;

        PitcherRecord record = PitcherRecord.builder()
                .scheduleId(scheduleId)
                .teamId(teamId)
                .playerId(playerId)
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
    
    public List<BatterRecordDTO> getBatterRecords(int scheduleId, int teamId) {
        List<BatterRecord> records = batterRecordRepository.findByScheduleIdAndTeamId(scheduleId, teamId);
        List<BatterRecordDTO> dtoList = new ArrayList<>();

        for (BatterRecord r : records) {
            BatterRecordDTO dto = BatterRecordDTO.builder()
                .playerId(r.getPlayer().getId())
                .playerName(r.getPlayer().getName())
                .pa(r.getPa())
                .ab(r.getAb())
                .hits(r.getHits())
                .rbi(r.getRbi())
                .hr(r.getHr())
                .sb(r.getSb())
                .so(r.getSo())
                .bb(r.getBb())
                .build();
            dtoList.add(dto);
        }

        return dtoList;
    }

    public List<PitcherRecordDTO> getPitcherRecords(int scheduleId, int teamId) {
        List<PitcherRecord> records = pitcherRecordRepository.findByScheduleIdAndTeamId(scheduleId, teamId);
        List<PitcherRecordDTO> dtoList = new ArrayList<>();

        for (PitcherRecord r : records) {
            PitcherRecordDTO dto = PitcherRecordDTO.builder()
                .playerId(r.getPlayer().getId())
                .playerName(r.getPlayer().getName())
                .innings(r.getInnings())
                .strikeouts(r.getStrikeouts())
                .bb(r.getBb())
                .hbp(r.getHbp())
                .runs(r.getRuns())
                .earnedRuns(r.getEarnedRuns())
                .hits(r.getHits())
                .hr(r.getHr())
                .decision(r.getDecision())
                .build();
            dtoList.add(dto);
        }

        return dtoList;
    }
    
    public List<PitcherRecordDTO> getAllPitcherRecordsByScheduleId(int scheduleId) {
        List<PitcherRecord> entities = pitcherRecordRepository.findByScheduleId(scheduleId);
        List<PitcherRecordDTO> dtoList = new ArrayList<>();

        for (PitcherRecord entity : entities) {
            dtoList.add(PitcherRecordDTO.builder()
                    .scheduleId(entity.getScheduleId())
                    .teamId(entity.getTeamId())
                    .playerId(entity.getPlayerId())
                    .playerName(entity.getPlayer().getName())
                    .innings(entity.getInnings())
                    .strikeouts(entity.getStrikeouts())
                    .bb(entity.getBb())
                    .hbp(entity.getHbp())
                    .runs(entity.getRuns())
                    .earnedRuns(entity.getEarnedRuns())
                    .hits(entity.getHits())
                    .hr(entity.getHr())
                    .decision(entity.getDecision())
                    .build());
        }

        return dtoList;
    }
    
    public List<String> getPitcherNamesByScheduleId(int scheduleId, int teamId) {
        return pitcherRecordRepository.findPitcherNamesByScheduleIdAndTeamId(scheduleId, teamId);
    }
}
