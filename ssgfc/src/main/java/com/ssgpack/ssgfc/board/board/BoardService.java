package com.ssgpack.ssgfc.board.board;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class BoardService {

    @Autowired
    private BoardRepository br;

    // 게시글 전체 조회
    public List<Board> findAll() {
        return br.findAll();
    }

    // 게시글 상세 조회 + 조회수 증가
    public Board find(Long id) {
        Board bd = br.findById(id).orElseThrow();
        bd.setHit(bd.getHit() + 1);
        br.save(bd);
        return bd;
    }

    // 게시글 저장
    public void insert(Board bd, Long member_id) {
        bd.setIp();
        br.save(bd);
    }

    // 게시글 삭제
    public void delete(Long id) {
        br.deleteById(id);
    }

    // 게시글 단순 조회 (조회수 증가 없음)
    public Board findById(Long id) {
        return br.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없음"));
    }

    // 게시글 수정 (update)
    public void update(Long id, Board updatedBoard) {
        Board board = br.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id=" + id));

        board.setTitle(updatedBoard.getTitle());
        board.setContent(updatedBoard.getContent());
        board.setImg(updatedBoard.getImg());

        br.save(board);
    }
}
