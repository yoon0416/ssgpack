package com.ssgpack.ssgfc.game;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

@Getter
@Entity
@Table(name = "game_schedule")
public class GameSchedule {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate gameDate;
	
	public LocalDate getGameDate() { return gameDate; }
	
	private String location;
	
	private String result;
	
	public String getResult() { return result; }
	
	@Column(columnDefinition = "TEXT")
	private String report;
	
	private String team1;
	
	public String getTeam1() { return team1; }
	
	private String team2;
	
	public String getTeam2() { return team2; }
	
	private Integer score1;
	
	private Integer score2;
	
	public GameSchedule() {}
	
	public GameSchedule(LocalDate gameDate, String location, String result, String report,
						String team1 , String team2, Integer score1, Integer score2)	{
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