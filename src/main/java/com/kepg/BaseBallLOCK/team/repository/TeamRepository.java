package com.kepg.BaseBallLOCK.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.team.domain.Team;

public interface TeamRepository extends JpaRepository<Team, Integer>{
	
    @Query("SELECT t.color FROM Team t WHERE t.id = :id")
    String findColorById(@Param("id") int id);
    
    
}
