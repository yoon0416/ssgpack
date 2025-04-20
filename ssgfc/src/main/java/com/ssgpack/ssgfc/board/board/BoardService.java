package com.ssgpack.ssgfc.board.board;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.transaction.Transactional;

@Service
@Transactional
public class BoardService {

    @Autowired
    private BoardRepository br;

    public List<Board> findAll() {
        return br.findAll();
    }

    public Board find(Long id) {
        Board bd = br.findById(id).orElseThrow();
        bd.setHit(bd.getHit() + 1);
        br.save(bd);
        return bd;
    }

    public void insert(Board bd, Long member_id) {
        bd.setIp();
        br.save(bd);
    }

    public void delete(Long id) {
        br.deleteById(id);
    }
    
    public Board findById(Long id) {
        return br.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없음"));
    }
}
