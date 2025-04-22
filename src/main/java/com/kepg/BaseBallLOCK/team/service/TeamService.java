// TeamService.java
package com.kepg.BaseBallLOCK.team.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.team.domain.Team;
import com.kepg.BaseBallLOCK.team.repository.TeamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    public Team getTeamById(int id) {
        Optional<Team> optional = teamRepository.findById(id);
        return optional.orElse(null);
    }
   
}

