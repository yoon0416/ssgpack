package com.ssgpack.ssgfc.user;

import java.io.Serializable;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.comment.Comment;
import com.ssgpack.ssgfc.board.like.Like;
import com.ssgpack.ssgfc.vote.UserVote;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity  
@Getter  
@Setter 
@ToString(exclude = "board")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    // 이메일 (유니크, 필수)
    @Column(unique = true, nullable = false)
    private String email;

    // 닉네임 (중복허용, 필수)
    @Column(nullable = false)
    private String nick_name;

    // 비밀번호
    @Column(nullable = false)
    private String pwd;

    // 가입 IP (최초만 저장)
    @Column(updatable = false)
    private String ip;

    // 가입 일자
    @Column(updatable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    // 권한 (admin=0~4, member=5)
    private int role;

    // 카카오 로그인 시 부여된 ID
    @Column(name = "kakao_id", unique = true, nullable = true)
    private Long kakaoId;
    
    // 구글 로그인 시 부여된 ID
    @Column(name = "google_id", unique = true, nullable = true)
    private String googleId;  // 구글 고유 ID
    
    // 네이버 로그인 시 부여된 ID
    @Column(name = "naver_id", unique = true, nullable = true)
    private String naverId;


    // 소개글
    @Column(columnDefinition = "TEXT")
    private String introduce;

    // 프로필 이미지 파일명
    @Column(name = "profile_img")
    private String profile_img;

    // 주소
    @Column(name = "zipcode")
    private String zipcode; // 우편번호

    @Column(name = "address")
    private String address; // 기본 주소

    @Column(name = "address_detail")
    private String addressDetail; // 상세 주소

    // 이메일 인증 여부
    @Column(name = "email_chk", nullable = false)
    private boolean email_chk = false;

    //전화번호
    @Column(name = "phone", length = 20)
    private String phone;

    // 연관 게시글
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Board> board = new ArrayList<>();

    // 연관 댓글
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 좋아요 기록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();
    
    // 투표 기록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserVote> userVotes = new ArrayList<>();

    // IP 자동 설정 메서드
    public void setIp() {
        try {
            this.ip = InetAddress.getLocalHost().getHostAddress();
        } catch(Exception e) {
            e.printStackTrace();
            this.ip = null;
        }
    }
    
    // profileImg로 접근할 수 있게 Getter 추가
    public String getProfileImg() {
        return this.profile_img;
    }
    
    //뱃지
    public String getSocialType() {
        if (this.kakaoId != null) {
            return "카카오";
        } else if (this.googleId != null) {
            return "구글";
        } else if (this.naverId != null) {
            return "네이버";
        } else {
            return "일반";
        }
    }
    
    // 인증된거 체크
    public boolean isVerified() {
        return this.email_chk
            || this.kakaoId != null
            || this.googleId != null
            || this.naverId != null
            || (this.phone != null && !this.phone.trim().isEmpty());
    }


}
