package com.ssgpack.ssgfc.player;

import javax.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;      // 선수 이름
    private String position;  // 팀 또는 포지션
    private double avg;       // 타율 (AVG)
}