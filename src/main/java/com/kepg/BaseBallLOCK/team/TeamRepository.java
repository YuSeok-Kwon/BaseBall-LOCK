package com.kepg.BaseBallLOCK.team;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kepg.BaseBallLOCK.team.teamDomain.Team;

public interface TeamRepository extends JpaRepository<Team, Integer>{

}
