package com.ssgpack.ssgfc.board.board;

import com.ssgpack.ssgfc.board.api.SentimentService;
import com.ssgpack.ssgfc.board.like.LikeService;
import com.ssgpack.ssgfc.util.UtilUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BoardService {

    @Autowired
    private BoardRepository br;

    @Autowired
    private UtilUpload utilUpload;

    @Autowired
    private LikeService likeService;

    @Autowired
    private SentimentService sentimentService;

    // ✅ 공지글만 조회
    public List<Board> findNoticeBoards() {
        return br.findByTitleStartingWithOrderByCreateDateDesc("[공지]");
    }

    // ✅ DTO 기반 게시글 조회 (검색 포함)
    public Page<BoardListDto> getBoardListWithCounts(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return br.findAllBoardListWithCounts(keyword, pageable); // keyword 포함
        } else {
            return br.findAllBoardListWithCounts(pageable); // 일반 목록
        }
    }

    // ✅ 페이징 정보 생성
    public PagingDto createPagingDto(Page<BoardListDto> boardPage) {
        return new PagingDto((int) boardPage.getTotalElements(), boardPage.getNumber());
    }

    // ✅ 게시글 단건 조회
    public Board find(Long id) {
        return br.findById(id).orElseThrow();
    }

    // ✅ 조회수 증가
    public void increaseViewCount(Long id) {
        Board board = br.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        board.setHit(board.getHit() + 1);
        br.save(board);
    }

    // ✅ 게시글 등록
    public void insert(Board bd, Long member_id, MultipartFile file) throws IOException {
        bd.setIp();

        if (!file.isEmpty()) {
            String savedName = utilUpload.fileUpload(file, "board/");
            bd.setImg(savedName);
        }

        String emotion = sentimentService.analyzeSentiment(bd.getContent());
        bd.setEmotion(emotion);

        br.save(bd);
    }

    // ✅ 게시글 수정
    public void update(Long id, Board updatedBoard, MultipartFile file) throws IOException {
        Board board = br.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id=" + id));

        board.setTitle(updatedBoard.getTitle());
        board.setContent(updatedBoard.getContent());

        if (!file.isEmpty()) {
            String savedName = utilUpload.fileUpload(file, "board/");
            board.setImg(savedName);
        }

        String newEmotion = sentimentService.analyzeSentiment(updatedBoard.getContent());
        board.setEmotion(newEmotion);

        br.save(board);
    }

    // ✅ 게시글 삭제
    public void delete(Long id) {
        br.deleteById(id);
    }

    // ✅ DTO 기반 인기글 조회 (쿼리 최적화)
    public List<BoardListDto> getPopularBoards(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return br.findTopPopularBoards(pageable);
    }

    // ✅ 수정 시 조회
    public Board findById(Long id) {
        return br.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없음"));
    }
    
    // ✅ NULL 체크
    public Board findByIdOrNull(Long id) {
        return br.findById(id).orElse(null);
    }
}
