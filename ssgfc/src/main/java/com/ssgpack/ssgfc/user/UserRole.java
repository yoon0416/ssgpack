package com.ssgpack.ssgfc.user;

import lombok.Getter;

@Getter
public enum UserRole {

    // enum 상수 정의 (권한 이름, 코드 값)
    MASTER(0, "ROLE_MASTER", "마스터 관리자"),
    USER_MANAGER(1, "ROLE_USER_MANAGER", "유저 관리자"),
    PLAYER_MANAGER(2, "ROLE_PLAYER_MANAGER", "선수 관리자"),
    BOARD_MANAGER(3, "ROLE_BOARD_MANAGER", "게시판 관리자"),
    GAME_MANAGER(4, "ROLE_GAME_MANAGER", "경기일정 관리자"),
    MEMBER(5, "ROLE_MEMBER", "일반 사용자");

    private final int code;           // 숫자 코드
    private final String roleName;    // ROLE_XXX (Spring Security용)
    private final String displayName; // 화면에 표시할 한글 이름

    UserRole(int code, String roleName, String displayName) {
        this.code = code;
        this.roleName = roleName;
        this.displayName = displayName;
    }

    public static UserRole fromCode(int code) {
        for (UserRole role : values()) {
            if (role.code == code) return role;
        }
        throw new IllegalArgumentException("Invalid role code: " + code);
    }

    public static String getDisplayNameByCode(int code) {
        for (UserRole role : values()) {
            if (role.code == code) {
                return role.displayName;
            }
        }
        return "알 수 없음"; // 코드 없을 때 대비
    }
}
