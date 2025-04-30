package com.ssgpack.ssgfc.review;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ✅ 특정 날짜에 리뷰가 존재하는지 확인하는 메서드
    boolean existsByGameDate(LocalDate gameDate);
    Optional<Review> findFirstByGameDate(LocalDate gameDate);
    boolean existsByGameUrlAndHowAndResult(String gameUrl, String how, String result);
    List<Review> findAllByGameDate(LocalDate date);
    Optional<Review> findByGameUrlAndHowAndResult(String gameUrl, String how, String result);
    Optional<Review> findFirstByGameDateAndHow(LocalDate gameDate, String how);
    List<Review> findAllByGameDateAndHowNot(LocalDate gameDate, String how);
    @Query("SELECT DISTINCT FUNCTION('DATE_FORMAT', r.gameDate, '%Y-%m') FROM Review r ORDER BY 1")
    List<String> findDistinctMonths();
}