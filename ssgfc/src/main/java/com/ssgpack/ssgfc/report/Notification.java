package com.ssgpack.ssgfc.report;

import com.ssgpack.ssgfc.user.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 알림 받는 관리자

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 알림 내용

    @Column(nullable = false) // ✅ 추가
    private Long reportId; // ✅ 연결된 신고 ID

    @Column(updatable = false)
    private LocalDateTime createdDate; // 생성일자

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }
}
