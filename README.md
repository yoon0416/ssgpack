# ⚾ SSG 응원 커뮤니티

**SSG 랜더스 팬들을 위한 올인원 커뮤니티 플랫폼**  

> ✅ 로그인 기반 커뮤니티  
> ✅ 관리자 기능 완비  
> ✅ 경기 요약 + 감정 분석 + AI 평가 기능 탑재  
> ✅ 전화번호/이메일 인증 + 로그 시스템

## 🔍 관련문서 바로가기
- [버전 히스토리 보기](./version.md)
- [트러블슈팅 목록](https://github.com/yoon0416/ssgpack/blob/main/%ED%8A%B8%EB%9F%AC%EB%B8%94%EC%8A%88%ED%8C%85.md)
- [시큐리티 업그레이드 예정](https://github.com/yoon0416/ssgpack/blob/main/%EC%8B%9C%ED%81%90%EB%A6%AC%ED%8B%B0.md)
- 기능구현영상
  - [안윤기_ user+시큐리티+](https://youtu.be/pWBEOX9JKqc)
  - [박인_ board+vote+신고+](https://youtu.be/ePs5JN8RhW0)
  - [김재원_ 경기 데이터 크롤링+날씨api+](https://youtu.be/nX0wIDsvyUM)
  - [조율비_ 선수 데이터 크롤링+뉴스api+](https://www.youtube.com/watch?v=q3qigNTTAMc)

---

## 🔥 주요 기능

| 기능 | 설명 |
|--------|-------|
| 👤 회원가입/로그인 | 커스텀 로그인, 카카오/네이버/구글 간편 로그인, 이메일/전화 인증 포함 |
| 🏟️ 게시판 | 감정 분석 기반 배경색 적용, 이미지 업로드, 신고 기능 포함 |
| 🗳️ 팬 투표 | 관리자 생성 / 유저 참여 / 결과 확인 가능 |
| 🧑‍💼 관리자 기능 | 유저 / 게시판 / 투표 / 선수 / 경기일정 / 신고 관리 역할별 분리 |
| ⚾ 선수 목록 | 관리자 등록, AI 감정 평가 실행 기능 포함 |
| 🗓️ 경기 요약 | 오늘 날짜 기준 자동 요약 + 관리자 일정 등록 기능 포함 |
| 🌤️ 날씨 정보 | 기상청 초단기 API 연동 (온도, 강수확률, 풍속 등 시각화) |
| 📄 로그 기록 | 기능별/날짜별 자동 로그 저장, 30일 이후 자동 삭제 처리 |

---

## 🔒 인증 및 보안
- **Spring Security 기반 커스텀 로그인 구조 구현**
- 사용자 역할(Role)에 따라 기능 접근 제어
  - 일반 유저: 글 작성/댓글/투표 참여
  - 관리자: 유저/게시판/선수/일정/신고/투표 관리
- 본인만 게시글 수정/삭제 가능
- 비로그인 시 기능 제한

---

## 🛠️ 사용 기술 스택
- **백엔드**: Java 11, Spring Boot 2.7.14, JPA(Hibernate), Maven
- **프론트엔드**: Thymeleaf, Bootstrap, JavaScript
- **DB/서버**: MySQL 8
- **보안/인증**:
  - Spring Security (커스텀 인증 처리)
  - 소셜 로그인: Kakao, Naver, Google
  - 이메일 인증: Google SMTP
  - 전화번호 인증: CoolSMS API
- **외부서비스 연동**:
  - 기상청 초단기 예보 API (날씨)
  - Kakao 우편번호 서비스 (주소 입력 자동화)
- **크롤링**:
  - Jsoup 기반 크롤링 (선수 목록, 경기 일정)

---

## 📆 크롤링 / 외부 API 목록

| 대상 | 방식 / 출처 |
|--------|-------------|
| 선수 목록 | Jsoup + Statiz 기반 크롤링 |
| 경기 일정 | SSG 공식사이트 HTML 크롤링 |
| 날씨 정보 | 기상청 초단기 API (온도, 강수확률, 풍속 등) |
| 비밀번호 메일 | Naver SMTP / Google Gmail SMTP API |
| 소셜 로그인 | Kakao, Naver, Google OAuth2 API |
| 전화 인증 | CoolSMS 인증 API |
| 주소 자동입력 | Kakao 우편번호 API |
| 선수 목록 | Jsoup + [Statiz](https://statiz.sporki.com/) 기반 크롤링 |

---

## 📂 프로젝트 구조 (v2.0.0 기준)

```
com.ssgpack.ssgfc
├── admin              # 관리자 페이지 (유저, 게시판, 투표, 선수, 경기일정, 신고 관리)
├── board              # 게시판 도메인 (감정 분석, 신고 기능 포함)
├── user               # 회원 정보, 로그인/인증 처리 (이메일/전화번호)
├── vote               # 투표 시스템 도메인
├── schedule           # 경기 일정 및 경기 요약 기능
├── weather            # 날씨 API 처리 (기상청 연동)
├── logs               # 로그 기록 및 자동 삭제 스케줄러
├── player             # 선수 정보 및 AI 감정 평가 처리
└── SsgfcApplication   # 메인 실행 클래스
```

---


