package com.kepg.BaseBallLOCK.game.schedule.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime matchDate;

    private Integer homeTeamId;

    private Integer homeTeamScore;
    
    private Integer awayTeamId;
    
    private Integer awayTeamScore;


    @Column(length = 30)
    private String stadium;

    @Column(length = 20)
    private String status;
}
