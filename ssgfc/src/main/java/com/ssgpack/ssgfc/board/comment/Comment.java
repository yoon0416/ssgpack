package com.ssgpack.ssgfc.board.comment;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.user.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 댓글 내용
    private String content;

    // ✅ 작성자 IP
    private String ip;

    // ✅ 작성일자
    private LocalDateTime createDate;

    // ✅ 댓글 작성자
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // ✅ 해당 댓글이 속한 게시글
    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    // ✅ 부모 댓글 (대댓글의 경우만 설정, 원댓글은 null)
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // ✅ 대댓글 목록 (조회 전용)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Comment> children = new ArrayList<>();

    // ✅ 저장 시 자동 생성일자 입력
    @PrePersist
    protected void onCreate() {
        this.createDate = LocalDateTime.now();
    }
}
