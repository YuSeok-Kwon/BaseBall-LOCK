package com.kepg.BaseBallLOCK.team.teamRanking.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teamRanking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer teamId;

    private Integer season;

    private Integer ranking;

    private Integer games;

    private Integer wins;

    private Integer losses;

    private Integer draws;

    private Double winRate;

    private Double gamesBehind;
    

}
