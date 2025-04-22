package com.kepg.BaseBallLOCK.player.stats.domain;

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
@Table(name = "pitcherStats")
@Getter 
@Setter 
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class PitcherStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer playerId;

    private Integer season;

    private String category;

    private Double value;

    private Integer ranking;

    private String position;
}
