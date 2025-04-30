package com.ssgpack.ssgfc.report;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ssgpack.ssgfc.user.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // ✅ 신고 연결용 알림 저장 (user + content + reportId까지 저장)
    public void saveNotification(User user, String content, Long reportId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setContent(content);
        notification.setReportId(reportId);
        notificationRepository.save(notification);
    }

    // ✅ 신고 ID로 알림 삭제
    @Transactional
    public void deleteByReportId(Long reportId) {
        notificationRepository.deleteByReportId(reportId);
    }

    // ✅ 사용자 알림 목록 조회 (최신순)
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(User user) {
        return notificationRepository.findByUserIdOrderByCreatedDateDesc(user.getId());
    }
}

