package com.ssgpack.ssgfc.review;

import lombok.*;

import javax.persistence.*;

import com.ssgpack.ssgfc.game.GameSchedule;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String how;
    private String result;

    @Column(name = "game_url")
    private String gameUrl;

    // ✅ 날짜로 존재 여부 확인하려면 필요
    @Column(name = "game_date")
    private LocalDate gameDate;
    
    @Column(length = 1000)
    private String summary;
    
    @OneToOne
    @JoinColumn(name = "game_schedule_id")  // FK 위치
    private GameSchedule gameSchedule;

    
}