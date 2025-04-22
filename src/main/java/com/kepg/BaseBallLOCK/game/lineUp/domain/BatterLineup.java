package com.kepg.BaseBallLOCK.game.lineUp.domain;

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
@Table(name = "batterLineup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatterLineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer scheduleId;

    private Integer teamId;

    private Integer playerId;

    @Column(name = "`order`")
    private Integer order;

    @Column(length = 10)
    private String position;
}
