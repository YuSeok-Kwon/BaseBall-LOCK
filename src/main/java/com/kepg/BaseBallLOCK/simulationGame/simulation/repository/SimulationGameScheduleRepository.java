package com.kepg.BaseBallLOCK.simulationGame.simulation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kepg.BaseBallLOCK.simulationGame.simulation.domain.SimulationGameSchedule;

public interface SimulationGameScheduleRepository extends JpaRepository<SimulationGameSchedule, Integer> {

}
