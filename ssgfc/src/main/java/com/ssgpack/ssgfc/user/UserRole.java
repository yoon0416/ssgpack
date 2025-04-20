package com.ssgpack.ssgfc.user;

import lombok.Getter;

@Getter
public enum UserRole {

    // enum 상수 정의 (권한 이름, 코드 값)
    MASTER(0, "ROLE_MASTER"),                 // 최상위 관리자 (전체 시스템 관리), 마스터관리자
    USER_MANAGER(1, "ROLE_USER_MANAGER"),     // 사용자 관리 권한 관리자
    PLAYER_MANAGER(2, "ROLE_PLAYER_MANAGER"), // 선수 정보 담당 관리자
    BOARD_MANAGER(3, "ROLE_BOARD_MANAGER"),   // 게시판 담당 관리자
    GAME_MANAGER(4, "ROLE_GAME_MANAGER"),     // 경기 일정 담당 관리자
    MEMBER(5, "ROLE_MEMBER");                 // 일반 사용자

    // enum 각 상수가 갖는 속성 값들
    private final int code;          // DB 저장용 숫자 코드
    private final String roleName;   // Spring Security에서 사용되는 권한명 (ROLE_XXX)

    // 생성자: enum 상수마다 code, roleName 설정
    UserRole(int code, String roleName) {
        this.code = code;
        this.roleName = roleName;
    }

    // enum 값에서 숫자 코드 가져오는 메서드
    public int getCode() {
        return code;
    }

    // enum 값에서 문자열 ROLE 이름 가져오는 메서드
    public String getRoleName() {
        return roleName;
    }

    // 숫자 코드(int)로부터 enum 객체 찾아주는 정적 메서드
    public static UserRole fromCode(int code) {
        for (UserRole role : values()) {
            if (role.code == code) return role; // 해당 코드와 일치하는 enum 반환
        }
        throw new IllegalArgumentException("Invalid role code: " + code); // 없는 값이면 에러
    }
}