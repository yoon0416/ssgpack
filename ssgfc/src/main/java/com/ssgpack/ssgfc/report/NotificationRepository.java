package com.ssgpack.ssgfc.report;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ✅ 특정 사용자(User) 알림 목록 조회
    List<Notification> findByUserIdOrderByCreatedDateDesc(Long userId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.reportId = :reportId")
    void deleteByReportId(Long reportId);
}
