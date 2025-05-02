package com.kepg.BaseBallLOCK.review.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.review.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    
	List<Review> findByUserIdAndCreatedAtBetween(int userId, LocalDateTime start, LocalDateTime end);

	@Query("""
			SELECT r FROM Review r
			JOIN Schedule s ON r.scheduleId = s.id
			WHERE r.userId = :userId
			AND s.matchDate BETWEEN :start AND :end
			""")
			List<Review> findByUserIdAndScheduleMatchDateBetween(
			    @Param("userId") int userId,
			    @Param("start") LocalDateTime start,
			    @Param("end") LocalDateTime end
			);
	
	@Query("""
		    SELECT r FROM Review r 
		    JOIN Schedule s ON r.scheduleId = s.id 
		    WHERE r.userId = :userId 
		    AND DATE(s.matchDate) = :matchDate
		""")
		Optional<Review> findByUserIdAndMatchDate(@Param("userId") int userId, @Param("matchDate") LocalDate matchDate);
	
	List<Review> findAllByUserIdAndScheduleId(int userId, int scheduleId);
	
}
