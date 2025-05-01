package com.ssgpack.ssgfc.game;

import java.time.LocalDate;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "game_schedule")
@Setter
public class GameSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate gameDate;

    @Column(length = 50)
    private String location;

    private String result;    

    @Column(columnDefinition = "TEXT")
    private String report;

    @Column(length = 20)
    private String team1;

    @Column(length = 20)
    private String team2;

    private Integer score1;

    private Integer score2;
    
    private String startTime;
    
    @ManyToOne
    @JoinColumn(name = "stadium_id")  // 외래키는 여기!
    private Location stadiumLocation;
    
    public void setResult(String result) {
        this.result = result;
    }

    public void setScore1(Integer score1) {
        this.score1 = score1;
    }

    public void setScore2(Integer score2) {
        this.score2 = score2;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public GameSchedule(LocalDate gameDate, String location, String result, String report,
                        String team1, String team2, Integer score1, Integer score2) {
        this.gameDate = gameDate;
        this.location = location;
        this.result = result;
        this.report = report;
        this.team1 = team1;
        this.team2 = team2;
        this.score1 = score1;
        this.score2 = score2;
    }
}
