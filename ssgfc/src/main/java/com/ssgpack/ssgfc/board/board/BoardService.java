package com.ssgpack.ssgfc.board.board;

import com.ssgpack.ssgfc.util.UtilUpload;
import com.ssgpack.ssgfc.board.like.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // ✅ 공지글 전용 조회
    public List<Board> findNoticeBoards() {
        return br.findByTitleStartingWithOrderByCreateDateDesc("[공지]");
    }

    // ✅ 일반 게시글 최신순 + 페이징 + 검색 처리
    public Page<Board> getPaging(int page, String keyword) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));
        if (keyword != null && !keyword.trim().isEmpty()) {
            return br.findExcludeNoticeByKeyword(keyword, pageable);
        }
        return br.findExcludeNotice(pageable);
    }

    // ✅ 전체 게시글 수 조회 (페이징 계산용)
    public long getTotalCount() {
        return br.count();
    }

    // ✅ 게시글 단건 조회 (조회수 방지용)
    public Board find(Long id) {
        return br.findById(id).orElseThrow();
    }

    // ✅ 조회수 증가 (중복 방지용)
    public void increaseViewCount(Long id) {
        Board board = br.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        board.setHit(board.getHit() + 1);
        br.save(board);
    }

    // ✅ 게시글 저장
    public void insert(Board bd, Long member_id, MultipartFile file) throws IOException {
        bd.setIp();
        if (!file.isEmpty()) {
            String savedName = utilUpload.fileUpload(file, "board/");
            bd.setImg(savedName);
        }
        br.save(bd);
    }

    // ✅ 게시글 삭제
    public void delete(Long id) {
        br.deleteById(id);
    }

    // ✅ 단순 조회 (게시글 수정 시 활용)
    public Board findById(Long id) {
        return br.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없음"));
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

        br.save(board);
    }

    // ✅ 인기글 조회 (조회수/좋아요/시간 기반 점수 계산)
    public List<Board> getPopularBoards(int limit) {
        List<Board> boards = br.findAll();

        return boards.stream()
                .map(board -> {
                    long likeCount = likeService.countByBoard(board);
                    long hit = board.getHit();
                    long hours = Duration.between(board.getCreateDate(), LocalDateTime.now()).toHours();
                    double timeScore = Math.max(0, 48 - hours) * 0.5;
                    double score = (hit / 10.0) + (likeCount * 3) + timeScore;
                    board.setScore(score);
                    return board;
                })
                .sorted(Comparator.comparingDouble(Board::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
