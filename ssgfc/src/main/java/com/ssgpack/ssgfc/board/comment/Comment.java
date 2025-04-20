package com.ssgpack.ssgfc.board.comment;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.user.User;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

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

    private String content;

    private String ip;

    private LocalDateTime createDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;
    
    @PrePersist
    protected void onCreate() {
        this.createDate = LocalDateTime.now();
    }
}
