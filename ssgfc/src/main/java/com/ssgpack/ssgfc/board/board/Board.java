package com.ssgpack.ssgfc.board.board;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import com.ssgpack.ssgfc.board.comment.Comment;
import com.ssgpack.ssgfc.board.like.Like;
import com.ssgpack.ssgfc.user.User;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(exclude = "user")
public class Board {

    // ✅ PK
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 제목 (필수, 최대 200자)
    @NotBlank(message = "제목은 필수입니다.")
    @Column(length = 200, nullable = false)
    private String title;

    // ✅ 내용 (필수, TEXT 타입)
    @NotBlank(message = "내용은 필수입니다.")
    @Column(columnDefinition = "text", nullable = false)
    private String content;

    // ✅ 작성일 (변경 불가)
    @Column(updatable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    // ✅ 작성자 IP
    @Column(updatable = false)
    private String ip;

    // ✅ 조회수
    private Long hit = 0L;

    // ✅ 이미지 경로
    private String img;

    // ✅ 작성자
    @ManyToOne
    private User user;

    // ✅ 댓글 리스트
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // ✅ 좋아요 리스트
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<Like> likes = new ArrayList<>();

    // ✅ 인기글 점수 (DB에 저장되지 않음)
    @Transient
    private double score;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    // ✅ 작성자 IP 자동 세팅용
    public void setIp() {
        try {
            this.ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            this.ip = null;
        }
    }
}
