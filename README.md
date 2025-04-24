# SSG 응원 커뮤니티

팬들과 소통하고, SSG 랜더스를 응원하는 커뮤니티 플랫폼입니다.
경기일정 조회, 응원 게시판, 투표 시스템 등을 제공합니다.

📎 [트러블슈팅 모음 바로가기](https://github.com/yoon0416/ssgpack/blob/main/%ED%8A%B8%EB%9F%AC%EB%B8%94%EC%8A%88%ED%8C%85.md)

📦 [버전 히스토리 보기](https://github.com/yoon0416/ssgpack/blob/main/version.md)

---

## 📌 활용 사이트 및 활용코드
- [스탯티즈 - 선수 기록](https://statiz.sporki.com/?team=NC&year=2023)
- [SSG 랜더스 공식사이트 - 경기 일정](https://www.ssglanders.com/game/schedule)
- [쌤과 CRUD 정리](https://hi-sally03915.tistory.com/1724)
- [자바라이브러리정리](https://github.com/yoon0416/java_2025/blob/main/%EC%9E%90%EB%B0%94%20%EB%9D%BC%EC%9D%B4%EB%B8%8C%EB%9F%AC%EB%A6%AC%20%EC%A0%95%EB%A6%AC.md)
- [알고리즘정리초급](https://github.com/yoon0416/java_2025/blob/main/%EC%95%8C%EA%B3%A0%EB%A6%AC%EC%A6%98%20%EC%A0%95%EB%A6%AC(%EC%B4%88%EA%B8%89).md)
- [데이터정규화유튜브](https://youtu.be/Y1FbowQRcmI?si=uGIWDYPTpNVdiFUn)
- [NF 개념](https://github.com/yoon0416/java_2025/blob/main/NF%EA%B0%9C%EB%85%90.md)
- [부트스트랩](https://www.w3schools.com/bootstrap/bootstrap_ver.asp)
- [CoolSMS](https://console.coolsms.co.kr/)
---

## 🔥 주요 기능
- 회원가입 및 로그인 (간편 로그인 예정)
- 선수 목록 조회
  - 선수목록 크롤링으로 데이터 가져오기
- 응원 게시판 (CRUD)
- 경기일정/요약 조회
  - 경기일정 크롤링으로 데이터 가져오기
- 야구장 날씨 조회
  - 기상청 api 사용
- 팬 투표 시스템

---

## 🛡️ 보안 설정
- Spring Security 기반 로그인/로그아웃 구현
- 사용자 역할(Role)에 따라 접근 제어
  - 일반 유저: 게시글 작성/수정
  - 관리자: 선수/일정 등록, 관리자 페이지 접근 가능
- 본인만 글 수정/삭제 가능하도록 컨트롤러에서 인증 처리
- 로그인하지 않은 사용자는 게시글 작성/수정 불가능
  
---

## 🛠️ 사용 기술
- Java 11
- Spring Boot 2.7.14
- MySQL 8
- Thymeleaf
- Spring Security
- JPA (Hibernate)
- Maven

---

## 크롤링 (JSOUP 사용 아마?)
- 선수목록
- 경기일정

---

## 사용 api
- 네이버 메일 api
  - 유저가 비밀번호 찾기를 하면 서버는 유저의 이메일에 새 임시 pw를 줌
    - 유저 pw는 암호화를 하여 서버에서 못넘겨줌
- 카카오로그인 api
  - 간편 로그인
- 기상청 api
  - 야구에서 날씨는 중요한 포인트이기 때문에 경기장 날씨 보여주기

---

## 📂 프로젝트 구조 (25.04.20 기준)


com.ssgpack.ssgfc  
├── admin                        # 관리자 전용 컨트롤러  
│   └── AdminDashboardController.java  
├── board                        # 게시판 도메인  
│   ├── Board.java  
│   ├── BoardController.java  
│   ├── BoardService.java  
│   └── BoardRepository.java  
├── user                         # 사용자 및 인증 관련  
│   ├── User.java  
│   ├── UserController.java  
│   ├── UserService.java  
│   ├── UserRepository.java  
│   ├── CustomUserDetails.java  
│   ├── SecurityConfig.java  
│   └── UserDetailsService.java  
└── SsgfcApplication.java        # 메인 실행 파일

---

## 웹접근성에 맞게 홈페이지 구현
