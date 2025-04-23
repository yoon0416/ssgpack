package com.ssgpack.ssgfc.board.board;

import com.ssgpack.ssgfc.util.UtilUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class BoardService {

    @Autowired
    private BoardRepository br;

    @Autowired
    private UtilUpload utilUpload;

    // ✅ 전체 게시글 최신순 정렬 조회 (비페이징)
    public List<Board> findAll() {
        return br.findAllByOrderByCreateDateDesc();
    }

    // ✅ 페이징된 게시글 조회 (최신순 정렬 + Page 반환)
    public Page<Board> getPaging(int page, String keyword) {
        Pageable pageable = PageRequest.of(page, 10);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return br.findByTitleContainingOrContentContainingOrderByCreateDateDesc(keyword, keyword, pageable);
        }
        return br.findAllByOrderByCreateDateDesc(pageable);
    }

    // ✅ 전체 게시글 수 조회 (페이징 계산용)
    public long getTotalCount() {
        return br.count();
    }

    // ✅ 게시글 상세 조회 + 조회수 증가
    public Board find(Long id) {
        Board bd = br.findById(id).orElseThrow();
        bd.setHit(bd.getHit() + 1);
        br.save(bd);
        return bd;
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

    // ✅ 게시글 단순 조회
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
}
