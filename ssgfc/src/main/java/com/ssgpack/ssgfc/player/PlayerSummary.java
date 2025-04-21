package com.ssgpack.ssgfc.player;

// 중간 단계 데이터 처리용	크롤링 과정 중 1단계 결과를 일시적으로 저장
// DB에 저장할 구조 아님 	pno는 크롤링용 식별자일 뿐, DB 저장엔 필요 없음
// 후속 크롤링에 필요한 최소 정보	name, backNumber, pno만 있으면 상세 페이지 접근 가능

public class PlayerSummary {
	private String name;
	private String backNumber;
	private String pno;
	
	public PlayerSummary(String name, String backNumber, String pno) {
		this.name = name;
		this.backNumber = backNumber;
		this.pno = pno;
	}
}
