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

    @Column(nullable = false, length = 255)
    private String title;

    private LocalDateTime createDate;

    @OneToMany(mappedBy = "voteTitle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteContent> contents;

    @OneToMany(mappedBy = "voteTitle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserVote> userVotes;
}
