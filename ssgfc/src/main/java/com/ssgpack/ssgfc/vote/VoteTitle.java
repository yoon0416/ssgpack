// ✅ VoteTitle.java
package com.ssgpack.ssgfc.vote;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;

import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 투표 제목
    @Column(nullable = false, length = 255)
    private String title;

    // ✅ 생성일시
    private LocalDateTime createDate;

    // ✅ 마감일시
    private LocalDateTime endDate; // 👉 추가된 필드!

    // ✅ 이미지 파일 이름 (예: ssg_hero_vote.jpg)
    @Column(length = 255)
    private String img;

    // ✅ 투표 선택지들 (1:N 관계)
    @OneToMany(mappedBy = "voteTitle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteContent> contents;

    // ✅ 사용자 투표 기록들 (1:N 관계)
    @OneToMany(mappedBy = "voteTitle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserVote> userVotes;
}
