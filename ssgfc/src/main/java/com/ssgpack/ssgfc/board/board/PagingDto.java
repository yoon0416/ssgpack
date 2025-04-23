package com.ssgpack.ssgfc.board.board;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PagingDto {
    private int totalItems;    // 전체 게시글 수
    private int currentPage;   // 현재 페이지 (0부터 시작)
    private int pageSize = 10; // 한 페이지당 게시글 수
    private int totalPages;    // 전체 페이지 수

    private int startPage;     // 페이지바 시작 번호
    private int endPage;       // 페이지바 끝 번호
    private boolean hasPrev;   // 이전 버튼 표시 여부
    private boolean hasNext;   // 다음 버튼 표시 여부

    public PagingDto(int totalItems, int currentPage) {
        this.totalItems = totalItems;
        this.currentPage = currentPage;

        // 전체 페이지 수 계산
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);

        int blockSize = 10; // 페이지 번호 몇 개씩 보여줄지
        int displayPage = currentPage + 1; // 0부터 시작하는 currentPage → 실제로 보여줄 번호는 +1

        int currentBlock = (int) Math.ceil((double) displayPage / blockSize);
        this.startPage = (currentBlock - 1) * blockSize + 1;
        this.endPage = Math.min(startPage + blockSize - 1, totalPages);

        this.hasPrev = startPage > 1;
        this.hasNext = endPage < totalPages;
    }

    // 페이지네이션 버튼에서 사용할 번호 (1부터 시작하는 표시용 번호)
    public int getDisplayPage() {
        return currentPage + 1;
    }

    // 게시글 조회 시 사용할 offset (Spring에서는 사용하지 않지만 혹시 필요할 수 있음)
    public int getOffset() {
        return currentPage * pageSize;
    }

    public int getLimit() {
        return pageSize;
    }
}
