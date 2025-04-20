package com.kepg.BaseBallLOCK.player.stats.domain;

import com.kepg.BaseBallLOCK.player.domain.Player;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Player 연결 (다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerId", nullable = false)
    private Player player;

    private Integer season;

    @Column(length = 20)
    private String category;

    private Double value;

    private Integer ranking;
}