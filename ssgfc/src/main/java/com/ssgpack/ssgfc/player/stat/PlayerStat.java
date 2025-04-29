package com.ssgpack.ssgfc.player.stat;

import javax.persistence.*;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ssgpack.ssgfc.player.Player;

import lombok.*;

@Entity
@Table(name = "player_stat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PlayerStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String season;

    @Column(columnDefinition = "LONGTEXT")
    @Convert(converter = StatMapConverter.class)
    private Map<String, String> statMap;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    @JsonBackReference
    private Player player;
    

}


