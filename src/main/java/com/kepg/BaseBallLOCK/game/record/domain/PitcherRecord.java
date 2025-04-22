package com.kepg.BaseBallLOCK.game.record.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pitcherRecord")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PitcherRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer playerId;

    private Integer scheduleId;

    private Integer teamId;

    private Double innings;

    private Integer strikeouts;

    private Integer bb;

    private Integer hbp;

    private Integer runs;

    private Integer earnedRuns;

    private Integer hits;

    private Integer hr;

    @Column(length = 10)
    private String decision;
}
