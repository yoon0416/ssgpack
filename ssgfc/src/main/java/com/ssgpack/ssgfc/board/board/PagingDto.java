package com.ssgpack.ssgfc.board.board;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PagingDto {
    private int totalItems;     // 전체 게시글 수
    private int currentPage;    // 현재 페이지 (0부터 시작)
    private int pageSize = 10;  // 한 페이지당 게시글 수
    private int totalPages;     // 전체 페이지 수

    private int startPage;      // 페이지바 시작 번호
    private int endPage;        // 페이지바 끝 번호
    private boolean hasPrev;    // 이전 버튼 표시 여부
    private boolean hasNext;    // 다음 버튼 표시 여부

    public PagingDto(int totalItems, int currentPage) {
        this.totalItems = totalItems;
        this.currentPage = currentPage;

        // 총 페이지 수 계산
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);

        if (totalPages == 0) {
            // 🔒 게시글이 0개일 경우 기본값 고정
            this.startPage = 1;
            this.endPage = 1;
            this.hasPrev = false;
            this.hasNext = false;
        } else {
            int blockSize = 10; // 페이지바에 보여줄 페이지 번호 개수
            int displayPage = currentPage + 1; // 실제 표시용 (1부터 시작)

            int currentBlock = (int) Math.ceil((double) displayPage / blockSize);
            this.startPage = (currentBlock - 1) * blockSize + 1;
            this.endPage = Math.min(startPage + blockSize - 1, totalPages);

            this.hasPrev = startPage > 1;
            this.hasNext = endPage < totalPages;
        }
    }

    // 표시용 현재 페이지 번호 (1부터 시작)
    public int getDisplayPage() {
        return currentPage + 1;
    }

    // offset 계산 (Spring에서는 보통 사용 안 함)
    public int getOffset() {
        return currentPage * pageSize;
    }

    public int getLimit() {
        return pageSize;
    }
}
