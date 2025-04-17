package com.ssgpack.ssgfc.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "game_schedule")
public class GameSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_date", nullable = false)
    private LocalDate gameDate;

    @Column(nullable = false)
    private String team1;

    @Column(nullable = false)
    private String team2;

    @Column
    private String score1;

    @Column
    private String score2;

    @Column
    private String status; // 경기 예정 / 경기 결과 / 경기 취소

    public GameSchedule() {}

    public GameSchedule(LocalDate gameDate, String team1, String team2,
                        String score1, String score2, String status) {
        this.gameDate = gameDate;
        this.team1 = team1;
        this.team2 = team2;
        this.score1 = score1;
        this.score2 = score2;
        this.status = status;
    }

    // getter, setter
    public Long getId() {
        return id;
    }

    public LocalDate getGameDate() {
        return gameDate;
    }

    public void setGameDate(LocalDate gameDate) {
        this.gameDate = gameDate;
    }

    public String getTeam1() {
        return team1;
    }

    public void setTeam1(String team1) {
        this.team1 = team1;
    }

    public String getTeam2() {
        return team2;
    }

    public void setTeam2(String team2) {
        this.team2 = team2;
    }

    public String getScore1() {
        return score1;
    }

    public void setScore1(String score1) {
        this.score1 = score1;
    }

    public String getScore2() {
        return score2;
    }

    public void setScore2(String score2) {
        this.score2 = score2;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}