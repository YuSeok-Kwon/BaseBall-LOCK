package com.kepg.BaseBallLOCK.review.summary.repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kepg.BaseBallLOCK.review.domain.ReviewSummary;

public interface ReviewSummaryRepository extends JpaRepository<ReviewSummary, Integer> {

    Optional<ReviewSummary> findTopByUserIdOrderByWeekEndDateDesc(int userId);
    
    List<ReviewSummary> findByUserIdAndWeekStartDate(int userId, Date weekStartDate);
    
    boolean existsByUserIdAndWeekStartDate(int userId, Date weekStartDate);

}