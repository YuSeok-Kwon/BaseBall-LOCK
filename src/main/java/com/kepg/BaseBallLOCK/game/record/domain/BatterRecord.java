package com.kepg.BaseBallLOCK.game.record.domain;

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
@Table(name = "batterRecord")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatterRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer playerId;

    private Integer scheduleId;

    private Integer teamId;

    private Integer pa;

    private Integer ab;

    private Integer hits;

    private Integer rbi;

    private Integer hr;

    private Integer sb;

    private Integer so;

    private Integer bb;
}
