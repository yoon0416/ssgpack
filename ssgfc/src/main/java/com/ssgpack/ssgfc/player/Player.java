package com.ssgpack.ssgfc.player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ssgpack.ssgfc.player.stat.PlayerStat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "player")
@Getter
@Setter

public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pno")
    private String pno;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "back_number")
    private String backNumber;
    
    private LocalDate birthDate;

    private String school;

    private String draftInfo;

    private String activeYears;

    private String teams;
    
    private String position;
    
    @Column(name = "image_url")
    private String imageUrl;

    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PlayerStat> stats = new ArrayList<>();
    
    @Transient
    private int viewCount;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
}
