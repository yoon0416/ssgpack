package com.ssgpack.ssgfc.game;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stadium_location")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "stadiumLocation")
    private List<GameSchedule> gameSchedules;

    private String stadiumName;  // 전체 이름: 인천SSG랜더스필드
    private String shortName;    // 간략한 이름: 문학, 잠실
    private String teamKey;      // 마커용 키값: sk, doosan
    private String topPercent;   // 지도 top 위치: 28%
    private String leftPercent;  // 지도 left 위치: 38%
    private int nx;              // 날씨 API용 nx
    private int ny;              // 날씨 API용 ny
}