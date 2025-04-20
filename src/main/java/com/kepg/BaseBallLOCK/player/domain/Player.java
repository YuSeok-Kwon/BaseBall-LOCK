package com.kepg.BaseBallLOCK.player.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "player")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer teamId;

    @Column(length = 30, nullable = false)
    private String name;

    @Column(length = 10)
    private String position;

    private LocalDate birth;

    @Column(length = 10)
    private String hand;

    private Integer uniformNumber;

    private Integer season;
}
